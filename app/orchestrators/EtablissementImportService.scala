package orchestrators

import cats.implicits.toTraverseOps
import clients.GeoApiClient
import clients.InseeClient
import config.SignalConsoConfiguration
import controllers.error.EtablissementJobAleadyRunningError
import models.EnterpriseImportInfo
import models.ImportRequest
import models.api.GeoApiCommune
import models.insee.etablissement.DisclosedStatus
import models.insee.etablissement.Header
import models.insee.etablissement.InseeEtablissementResponse
import models.insee.etablissement.UniteLegale
import models.insee.token.InseeEtablissementQuery
import play.api.Logger
import repositories.insee.EtablissementRepositoryInterface
import repositories.entrepriseimportinfo.EnterpriseImportInfoRepository
import utils.Departments

import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.chaining.scalaUtilChainingOps

class EtablissementImportService(
    inseeClient: InseeClient,
    geoApiClient: GeoApiClient,
    repository: EtablissementRepositoryInterface,
    entrepriseImportRepository: EnterpriseImportInfoRepository,
    signalConsoConfiguration: SignalConsoConfiguration
)(implicit
    ec: ExecutionContext
) {

  private[this] val logger = Logger(this.getClass)

  // This fill the department column for every etablissement that still doesn't have it
  // We should only need to use this once, can be deleted after

  // Note : rows for which the department code can't be found (should be really rare)
  // are just ignored, but are then queried again by the next iteration.
  // It could be a problem if there are too many of them, for now we just try like that.
  def fillDepartementUntilAllDone(): Future[Unit] =
    for {
      rowsProcessed <- fillDepartmentWithLimit()
      _ <-
        if (rowsProcessed == 0) {
          logger.info("All etablissements filled with departments ")
          Future.successful(())
        } else {
          fillDepartementUntilAllDone()
        }
    } yield ()

  def fillDepartmentWithLimit(): Future[Int] =
    for {
      allCommunes    <- geoApiClient.getAllCommunes()
      etablissements <- repository.listWithoutMissingDepartment(1000)
      _ = logger.info(s"Filling codeDepartement for ${etablissements.length} etablissements")
      rowsSuccess <- etablissements.foldLeft(Future.successful(0)) { case (previous, etab) =>
        for {
          previousCount <- previous
          maybeCodeDepartment = Departments.findCodeDepartementOfEtablissement(Right(etab), allCommunes)
          rowsUpdatedSuccessfully <- maybeCodeDepartment match {
            case Some(codeDepartement) =>
              repository.updateDepartment(etab.siret, codeDepartement)
            case None =>
              logger.warn(s"Can't find departement of ${etab.siret} (CP ${etab.codePostalEtablissement})")
              Future.successful(0)
          }
        } yield previousCount + rowsUpdatedSuccessfully
      }
      _ = logger.info(s"Filled codeDepartement for $rowsSuccess/${etablissements.length} etablissements")
    } yield etablissements.length

  // One shot, API
  def runImportEtablissementsRequest(importRequest: ImportRequest): Future[Unit] =
    for {
      running <- entrepriseImportRepository.findRunning()
      _ <-
        if (running.nonEmpty) Future.failed(EtablissementJobAleadyRunningError(s"${running.size} jobs already running"))
        else Future.unit
      token <- inseeClient.generateToken()
      disclosedStatus =
        if (signalConsoConfiguration.publicDataOnly) {
          logger.warn(
            " !!!!!  Fetching disclosed public data only , check PUBLIC_DATA_ONLY env var for more information!!!!!"
          )
          Some(DisclosedStatus.Public)
        } else None
      allCommunes <- geoApiClient.getAllCommunes()
      query = InseeEtablissementQuery(
        token = token,
        beginPeriod = importRequest.begin,
        endPeriod = importRequest.end,
        siret = importRequest.siret,
        disclosedStatus = disclosedStatus
      )
      firstCall <- inseeClient.getEtablissement(query)
      _ = logger.info(s"Company count to update  = ${firstCall.header.total}")
      _ <- iterate(query, allCommunes)
    } yield ()

  def importEtablissements(): Future[Unit] =
    entrepriseImportRepository.create(EnterpriseImportInfo(linesCount = 0)).flatMap { batchInfo =>
      (for {
        _           <- validateIfRunning(batchInfo.id)
        allCommunes <- geoApiClient.getAllCommunes()
        query       <- computeQuery()
        firstCall   <- inseeClient.getEtablissement(query)
        _ = logger.info(s"Company count to update  = ${firstCall.header.total}")
        _ <- entrepriseImportRepository.updateLineCount(batchInfo.id, firstCall.header.total.toDouble)
        _ <- iterateThroughEtablissement(
          query,
          executionId = batchInfo.id,
          allCommunes
        )
        _ <- entrepriseImportRepository.updateEndedAt(batchInfo.id)
        _ = logger.info(s"Job ended successfully")
      } yield ()).recoverWith {
        case e: EtablissementJobAleadyRunningError =>
          logger.warn("Cannot run job", e)
          updateError(batchInfo.id, e)
        case e =>
          logger.error("Error on import", e)
          updateError(batchInfo.id, e)
      }
    }

  private def updateError(batchId: UUID, e: Throwable): Future[Unit] = entrepriseImportRepository
    .updateError(batchId, error = s"${e.getMessage} : ${e.getCause}")
    .flatMap(_ => Future.failed[Unit](e))

  private def validateIfRunning(current: UUID): Future[Unit] =
    for {
      before <- entrepriseImportRepository.findRunning().map(_.filterNot(_.id == current))
      _ = logger.info(s"Found ${before.size} running jobs, checking if lines are still updated")
      _ = logger.info(s"Waiting for them to update lines...")
      _ <- Future(Thread.sleep(65000))
      _ = logger.info(s"Getting back the same jobs")
      after <- entrepriseImportRepository.findRunning().map(_.filterNot(_.id == current))
      inter = after.intersect(before)
      _     = logger.info(s"Found ${inter.size} non closed jobs")
      _ <- inter
        .map(x =>
          entrepriseImportRepository
            .updateError(x.id, error = "Auto closed")
            .tap(_ => logger.info(s"Closing job with id ${x.id}"))
        )
        .sequence
      jobsRunning = after.diff(inter)
      res =
        if (jobsRunning.nonEmpty) {
          throw EtablissementJobAleadyRunningError(
            s"Jobs with id  ${jobsRunning.map(_.id.toString()).mkString(",")} already running"
          )
        } else {
          ()
        }
    } yield res

  private def computeQuery() =
    for {
      token           <- inseeClient.generateToken()
      lastExecutedJob <- entrepriseImportRepository.findLastEnded()
      beginPeriod = lastExecutedJob.flatMap(_.lastUpdated)
      disclosedStatus =
        if (signalConsoConfiguration.publicDataOnly) {
          logger.warn(
            " !!!!!  Fetching disclosed public data only , check PUBLIC_DATA_ONLY env var for more information!!!!!"
          )
          Some(DisclosedStatus.Public)
        } else None

      query = InseeEtablissementQuery(
        token = token,
        beginPeriod = beginPeriod,
        endPeriod = None,
        disclosedStatus = disclosedStatus
      )
      _ = logger.info(s"-------------  Running etablissement job with $query   ----------------")
    } yield query

  private def iterate(
      query: InseeEtablissementQuery,
      allCommunes: Seq[GeoApiCommune],
      header: Option[Header] = None
  ): Future[InseeEtablissementResponse] =
    for {
      etablissementResponse <- process(query, header, allCommunes)
      nextIteration <-
        if (etablissementResponse.header.nombre == InseeClient.EtablissementPageSize) {
          iterate(
            query,
            allCommunes,
            Some(etablissementResponse.header)
          )
        } else {
          Future.successful(etablissementResponse)
        }

    } yield nextIteration

  private def iterateThroughEtablissement(
      query: InseeEtablissementQuery,
      executionId: UUID,
      allCommunes: Seq[GeoApiCommune],
      header: Option[Header] = None,
      lineCount: Option[Int] = None
  ): Future[InseeEtablissementResponse] =
    for {
      etablissementResponse <- processEtablissement(
        query,
        executionId,
        header,
        lineCount,
        allCommunes
      )
      linesDone = lineCount.getOrElse(0) + InseeClient.EtablissementPageSize
      nextIteration <-
        if (etablissementResponse.header.nombre == InseeClient.EtablissementPageSize) {
          iterateThroughEtablissement(
            query,
            executionId,
            allCommunes,
            Some(etablissementResponse.header),
            Some(linesDone)
          )
        } else {
          Future.successful(etablissementResponse)
        }

    } yield nextIteration

  private def process(
      query: InseeEtablissementQuery,
      header: Option[Header],
      allCommunes: Seq[GeoApiCommune]
  ): Future[InseeEtablissementResponse] =
    for {
      etablissementResponse <- inseeClient
        .getEtablissement(
          query,
          cursor = header.flatMap(_.curseurSuivant)
        )
      _ <- insertOrUpdateEtablissements(etablissementResponse, allCommunes)
    } yield etablissementResponse

  private def processEtablissement(
      query: InseeEtablissementQuery,
      executionId: UUID,
      header: Option[Header],
      lineCount: Option[Int],
      allCommunes: Seq[GeoApiCommune]
  ): Future[InseeEtablissementResponse] =
    for {
      etablissementResponse <- process(query, header, allCommunes)
      lastUpdated = etablissementResponse.etablissements.lastOption.flatMap(e =>
        toOffsetDateTime(e.dateDernierTraitementEtablissement)
      )
      linesDone = lineCount.getOrElse(0) + etablissementResponse.header.nombre
      _ <- entrepriseImportRepository.updateLinesDone(executionId, linesDone.toDouble, lastUpdated)
      _ = logger.info(s"Processed $linesDone / ${etablissementResponse.header.total} lines so far")
    } yield etablissementResponse

  private def insertOrUpdateEtablissements(
      etablissementResponse: InseeEtablissementResponse,
      allCommunes: Seq[GeoApiCommune]
  ): Future[List[Int]] =
    etablissementResponse.etablissements.map { etablissement =>
      val denomination         = denominationFromUniteLegale(etablissement.uniteLegale)
      val maybeCodeDepartement = Departments.findCodeDepartementOfEtablissement(Left(etablissement), allCommunes)
      val companyData: Map[String, Option[String]] = etablissement.toMap(denomination, maybeCodeDepartement)
      repository.insertOrUpdate(companyData)
    }.sequence

  private[orchestrators] def denominationFromUniteLegale(uniteLegale: UniteLegale): String = {
    val fallbackName = s"${uniteLegale.prenomUsuelUniteLegale.getOrElse("")} ${uniteLegale.nomUsageUniteLegale
        .getOrElse(uniteLegale.nomUniteLegale.getOrElse(""))}"
    uniteLegale.denominationUniteLegale.getOrElse(fallbackName)
  }

}
