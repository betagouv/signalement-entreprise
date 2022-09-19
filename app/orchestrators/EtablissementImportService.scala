package orchestrators

import cats.implicits.toTraverseOps
import controllers.error.EtablissementJobAleadyRunningError
import models.EnterpriseImportInfo
import models.insee.etablissement.InseeEtablissement
import models.insee.etablissement.Header
import models.insee.etablissement.InseeEtablissementResponse
import models.insee.etablissement.UniteLegale
import models.insee.token.InseeEtablissementQuery
import play.api.Logger
import repositories.insee.EtablissementRepositoryInterface
import repositories.entrepriseimportinfo.EnterpriseImportInfoRepository

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.chaining.scalaUtilChainingOps

class EtablissementImportService(
    inseeClient: InseeClient,
    repository: EtablissementRepositoryInterface,
    entrepriseImportRepository: EnterpriseImportInfoRepository
)(implicit
    ec: ExecutionContext
) {

  private[this] val logger = Logger(this.getClass)

  def importEtablissement(): Future[Unit] =
    entrepriseImportRepository.create(EnterpriseImportInfo(linesCount = 0)).flatMap { batchInfo =>
      (for {
        _ <- validateIfRunning(batchInfo.id)
        query <- computeQuery()
        firstCall <- inseeClient.getEtablissement(query)
        _ = logger.info(s"Company count to update  = ${firstCall.header.total}")
        _ <- entrepriseImportRepository.updateLineCount(batchInfo.id, firstCall.header.total.toDouble)
        _ <- iterateThroughEtablissement(
          query,
          executionId = batchInfo.id
        )
        _ <- entrepriseImportRepository.updateEndedAt(batchInfo.id)
        _ = logger.info(s"Job ended successfully")
      } yield ()).recoverWith { case e =>
        logger.error("Error on import", e)
        entrepriseImportRepository
          .updateError(batchInfo.id, error = s"${e.getMessage} : ${e.getCause}")
          .flatMap(_ => Future.failed(e))
      }
    }

  private def validateIfRunning(current: UUID): Future[Unit] =
    for {
      before <- entrepriseImportRepository.findRunning().map(_.filterNot(_.id == current))
      _ = logger.info(s"Found ${before.size} running jobs, checking if lines are still updated")
      _ = logger.info(s"Waiting for them to update lines...")
      _ <- Future(Thread.sleep(65000))
      _ = logger.info(s"Getting back the same jobs")
      after <- entrepriseImportRepository.findRunning().map(_.filterNot(_.id == current))
      inter = after.intersect(before)
      _ = logger.info(s"Found ${inter.size} non closed jobs")
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
        } else { () }
    } yield res

  private def computeQuery() =
    for {
      token <- inseeClient.generateToken()
      lastExecutedJob <- entrepriseImportRepository.findLastEnded()
      beginPeriod = lastExecutedJob.flatMap(_.lastUpdated)
      query = InseeEtablissementQuery(
        token = token,
        beginPeriod = beginPeriod,
        endPeriod = None,
        disclosedStatus = None
      )
      _ = logger.info(s"-------------  Running etablissement job with $query   ----------------")
    } yield query

  private def iterateThroughEtablissement(
      query: InseeEtablissementQuery,
      executionId: UUID,
      header: Option[Header] = None,
      lineCount: Option[Int] = None
  ): Future[InseeEtablissementResponse] =
    for {
      etablissementResponse <- processEtablissement(
        query,
        executionId,
        header,
        lineCount
      )
      linesDone = lineCount.getOrElse(0) + InseeClient.EtablissementPageSize
      nextIteration <-
        if (etablissementResponse.header.nombre == InseeClient.EtablissementPageSize) {
          iterateThroughEtablissement(
            query,
            executionId,
            Some(etablissementResponse.header),
            Some(linesDone)
          )
        } else {
          Future.successful(etablissementResponse)
        }

    } yield nextIteration

  private def processEtablissement(
      query: InseeEtablissementQuery,
      executionId: UUID,
      header: Option[Header],
      lineCount: Option[Int]
  ) =
    for {
      etablissementResponse <- inseeClient
        .getEtablissement(
          query,
          cursor = header.flatMap(_.curseurSuivant)
        )
      _ <- insertOrUpdateEtablissements(etablissementResponse)
      lastUpdated = etablissementResponse.etablissements.lastOption.flatMap(
        _.dateDernierTraitementEtablissement.map(d => OffsetDateTime.of(LocalDateTime.parse(d), ZoneOffset.UTC))
      )
      linesDone = lineCount.getOrElse(0) + etablissementResponse.header.nombre
      _ <- entrepriseImportRepository.updateLinesDone(executionId, linesDone.toDouble, lastUpdated)
      _ = logger.info(s"Processed $linesDone / ${etablissementResponse.header.total} lines so far")
    } yield etablissementResponse

  private def insertOrUpdateEtablissements(etablissementResponse: InseeEtablissementResponse) =
    etablissementResponse.etablissements.map { etablissement =>
      val denomination = computeDenomination(etablissement)
      val companyData: Map[String, Option[String]] = etablissement.toMap(denomination)
      repository.insertOrUpdate(companyData)
    }.sequence

  private[orchestrators] def computeDenomination(etablissement: InseeEtablissement): String =
    etablissement.lastPeriodeEtablissement
      .flatMap(_.denominationUsuelleEtablissement)
      .getOrElse(denominationFromUniteLegale(etablissement.uniteLegale))

  private[orchestrators] def denominationFromUniteLegale(uniteLegale: UniteLegale): String = {

    val fallbackName =
      s"""${uniteLegale.prenomUsuelUniteLegale.getOrElse("")} 
         |${uniteLegale.nomUsageUniteLegale.getOrElse(uniteLegale.nomUniteLegale.getOrElse(""))}""".stripMargin

    uniteLegale.denominationUniteLegale
      .getOrElse(
        uniteLegale.denominationUsuelle1UniteLegale
          .getOrElse(
            uniteLegale.denominationUsuelle2UniteLegale
              .getOrElse(
                uniteLegale.denominationUsuelle3UniteLegale
                  .getOrElse(fallbackName)
              )
          )
      )
  }

}
