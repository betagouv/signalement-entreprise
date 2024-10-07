package orchestrators

import actors.InseeTokenActor
import cats.implicits.toTraverseOps
import clients.GeoApiClient
import clients.InseeClient
import config.SignalConsoConfiguration
import controllers.error.EtablissementJobAleadyRunningError
import controllers.error.InseeEtablissementError
import models.EnterpriseImportInfo
import models.ImportRequest
import models.api.GeoApiCommune
import models.insee.etablissement.DisclosedStatus
import models.insee.etablissement.Header
import models.insee.etablissement.InseeEtablissementResponse
import models.insee.etablissement.UniteLegale
import models.insee.token.InseeEtablissementQuery
import models.insee.token.InseeTokenResponse
import org.apache.pekko.actor
import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Scheduler
import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import org.apache.pekko.actor.typed.scaladsl.adapter.TypedSchedulerOps
import org.apache.pekko.util.Timeout
import play.api.Logger
import repositories.insee.EtablissementRepositoryInterface
import repositories.entrepriseimportinfo.EnterpriseImportInfoRepository
import utils.Departments
import org.apache.pekko.pattern.retry

import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.util.chaining.scalaUtilChainingOps

class EtablissementImportService(
    inseeTokenActor: ActorRef[InseeTokenActor.Command],
    inseeClient: InseeClient,
    geoApiClient: GeoApiClient,
    repository: EtablissementRepositoryInterface,
    entrepriseImportRepository: EnterpriseImportInfoRepository,
    signalConsoConfiguration: SignalConsoConfiguration
)(implicit
    ec: ExecutionContext,
    timeout: Timeout,
    scheduler: Scheduler
) {

  private[this] val logger                               = Logger(this.getClass)
  implicit private val classicScheduler: actor.Scheduler = scheduler.toClassic

  private def getInseeToken(
      request: ActorRef[InseeTokenActor.Reply] => InseeTokenActor.Command
  ): Future[InseeTokenResponse] =
    inseeTokenActor.ask[InseeTokenActor.Reply](request).flatMap {
      case InseeTokenActor.GotToken(token)   => Future.successful(token)
      case InseeTokenActor.TokenError(error) => Future.failed(error)
    }

  private def fetchDataFromInsee(
      query: InseeEtablissementQuery,
      cursor: Option[String] = None
  ): Future[InseeEtablissementResponse] = for {
    token    <- getInseeToken(InseeTokenActor.GetToken.apply)
    firstTry <- inseeClient.getEtablissement(token, query, cursor)
    result <- firstTry match {
      case Right(value) => Future.successful(value)
      case Left(InseeClient.TokenExpired) =>
        for {
          renewedAccessToken <- getInseeToken(InseeTokenActor.RenewToken.apply)
          secondTry          <- inseeClient.getEtablissement(renewedAccessToken, query, cursor)
          response <- secondTry match {
            case Right(value) => Future.successful(value)
            case Left(InseeClient.RateLimitExceeded) =>
              retry(() => fetchDataFromInsee(query, cursor), 2, 1.minute)
            case Left(_) => Future.failed(InseeEtablissementError("An issue occurred with the Insee token"))
          }
        } yield response
      case Left(InseeClient.RateLimitExceeded) =>
        retry(() => fetchDataFromInsee(query, cursor), 2, 1.minute)
    }
  } yield result

  // One shot, API
  def runImportEtablissementsRequest(importRequest: ImportRequest): Future[Unit] =
    for {
      running <- entrepriseImportRepository.findRunning()
      _ <-
        if (running.nonEmpty) Future.failed(EtablissementJobAleadyRunningError(s"${running.size} jobs already running"))
        else Future.unit
      disclosedStatus =
        if (signalConsoConfiguration.publicDataOnly) {
          logger.warn(
            " !!!!!  Fetching disclosed public data only , check PUBLIC_DATA_ONLY env var for more information!!!!!"
          )
          Some(DisclosedStatus.Public)
        } else None
      allCommunes <- geoApiClient.getAllCommunes()
      query = InseeEtablissementQuery(
        beginPeriod = importRequest.begin,
        endPeriod = importRequest.end,
        siret = importRequest.siret,
        disclosedStatus = disclosedStatus
      )
      firstCall <- fetchDataFromInsee(query)
      _ = logger.info(s"Company count to update  = ${firstCall.header.total}")
      _ <- iterate(query, allCommunes)
    } yield ()

  def importEtablissements(): Future[Unit] =
    entrepriseImportRepository.create(EnterpriseImportInfo(linesCount = 0)).flatMap { batchInfo =>
      (for {
        _           <- validateIfRunning(batchInfo.id)
        allCommunes <- geoApiClient.getAllCommunes()
        query       <- computeQuery()
        firstCall   <- fetchDataFromInsee(query)
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
      etablissementResponse <- fetchDataFromInsee(
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
