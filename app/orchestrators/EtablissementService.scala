package orchestrators

import cats.implicits.toTraverseOps
import company.EnterpriseImportInfo
import company.companydata.CompanyDataRepositoryInterface
import company.entrepriseimportinfo.EnterpriseImportInfoRepository
import models.insee.etablissement.Etablissement
import models.insee.etablissement.Header
import models.insee.etablissement.InseeEtablissementResponse
import models.insee.etablissement.UniteLegale
import models.insee.token.InseeEtablissementQuery
import play.api.Logger

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

trait EtablissementService {}

class EtablissementServiceImpl(
    inseeClient: InseeClient,
    repository: CompanyDataRepositoryInterface,
    entrepriseImportRepository: EnterpriseImportInfoRepository
)(implicit
    ec: ExecutionContext
) extends EtablissementService {

  private[this] val logger = Logger(this.getClass)

  def importEtablissement(): Future[Unit] =
    for {
      query <- computeQuery()
      firstCall <- inseeClient.getEtablissement(query)
      _ = logger.info(s"Company count to update  = ${firstCall.header.total}")
      batchInfo <- entrepriseImportRepository.create(EnterpriseImportInfo(linesCount = firstCall.header.total.toDouble))
      _ <- iterateThroughEtablissement(
        query,
        executionId = batchInfo.id
      ).recoverWith { case e =>
        logger.error("Error on import", e)
        entrepriseImportRepository
          .updateError(batchInfo.id, error = s"${e.getMessage} : ${e.getCause}")
          .flatMap(_ => Future.failed(e))
      }
      _ <- entrepriseImportRepository.updateEndedAt(batchInfo.id)
      _ = logger.info(s"Job ended successfully")
    } yield ()

  private def computeQuery() =
    for {
      token <- inseeClient.generateToken()
      lastExecutedJob <- entrepriseImportRepository.findLastEnded()
      beginPeriod = lastExecutedJob.flatMap(_.lastUpdated)
      query = InseeEtablissementQuery(
        token = token,
        beginPeriod = beginPeriod,
        endPeriod = None,
//        disclosedStatus = Some(DisclosedStatus.N)
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
      header: Option[Header] = None,
      lineCount: Option[Int] = None
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

  private[orchestrators] def computeDenomination(etablissement: Etablissement): String =
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
