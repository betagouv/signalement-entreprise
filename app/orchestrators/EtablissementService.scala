package orchestrators

import cats.implicits.toTraverseOps
import company.EnterpriseImportInfo
import company.companydata.CompanyDataRepositoryInterface
import company.entrepriseimportinfo.EnterpriseImportInfoRepository
import models.insee.etablissement.DisclosedStatus
import models.insee.etablissement.Etablissement
import models.insee.etablissement.Header
import models.insee.etablissement.InseeEtablissementResponse
import models.insee.etablissement.UniteLegale
import models.insee.token.InseeTokenResponse
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
      token <- inseeClient.generateToken()
      beginPeriod = OffsetDateTime.now().minusDays(10)
//      firstCall <- inseeClient.getEtablissement(token, disclosedStatus = Some(DisclosedStatus.N))
//      _ = logger.info(s"Company count to update  = ${firstCall.header.total}")
      batchInfo <- entrepriseImportRepository.create(EnterpriseImportInfo(linesCount = 0))
      _ <- iterateThroughEtablissement(
        beginPeriod = Some(beginPeriod),
        endPeriod = None,
        disclosedStatus = Some(DisclosedStatus.N),
        token = token,
        executionId = batchInfo.id
      ).recoverWith { case e =>
        logger.error("Error on import", e)
        entrepriseImportRepository
          .updateError(batchInfo.id, s"${e.getMessage} : ${e.getCause}")
          .flatMap(_ => Future.failed(e))
      }
      _ <- entrepriseImportRepository.updateEndedAt(batchInfo.id)
    } yield ()

  private def process(
      beginPeriod: Option[OffsetDateTime],
      endPeriod: Option[OffsetDateTime],
      disclosedStatus: Option[DisclosedStatus] = None,
      token: InseeTokenResponse,
      executionId: UUID,
      header: Option[Header] = None,
      lineCount: Option[Int] = None
  ) =
    for {
      etablissementResponse <- inseeClient
        .getEtablissement(
          token = token,
          beginPeriod = beginPeriod,
          disclosedStatus = disclosedStatus,
          endPeriod = endPeriod,
          cursor = header.flatMap(_.curseurSuivant)
        )
      _ = etablissementResponse.etablissements
        .map(e => (e.dateDernierTraitementEtablissement, e.dateCreationEtablissement))
        .foreach(println(_))
      _ <- processEtablissements(etablissementResponse)

      lastUpdated = etablissementResponse.etablissements.lastOption.flatMap(
        _.dateDernierTraitementEtablissement.map(d => OffsetDateTime.of(LocalDateTime.parse(d), ZoneOffset.UTC))
      )
      linesDone = lineCount.getOrElse(0) + InseeClient.EtablissementPageSize
      _ <- entrepriseImportRepository.updateLinesDone(executionId, linesDone.toDouble, lastUpdated)
      _ = logger.info(s"Processed $linesDone / ${etablissementResponse.header.total} lines so far")
    } yield etablissementResponse

  private def iterateThroughEtablissement(
      beginPeriod: Option[OffsetDateTime],
      endPeriod: Option[OffsetDateTime],
      disclosedStatus: Option[DisclosedStatus] = None,
      token: InseeTokenResponse,
      executionId: UUID,
      header: Option[Header] = None,
      lineCount: Option[Int] = None
  ): Future[InseeEtablissementResponse] =
    for {
      etablissementResponse <- process(beginPeriod, endPeriod, disclosedStatus, token, executionId, header, lineCount)
      linesDone = lineCount.getOrElse(0) + InseeClient.EtablissementPageSize
      nextIteration <-
        if (etablissementResponse.header.nombre == InseeClient.EtablissementPageSize) {
          iterateThroughEtablissement(
            beginPeriod,
            endPeriod,
            disclosedStatus,
            token,
            executionId,
            Some(etablissementResponse.header),
            Some(linesDone)
          )
        } else {
          Future.successful(etablissementResponse)
        }

    } yield nextIteration

  private def processEtablissements(etablissementResponse: InseeEtablissementResponse) =
    etablissementResponse.etablissements.map { etablissement =>
      val denomination = computeDenomination(etablissement)
      val companyData = etablissement.toMap(denomination)
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
