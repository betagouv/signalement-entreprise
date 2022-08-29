package orchestrators

import cats.implicits.toTraverseOps
import company.companydata.CompanyDataRepositoryInterface
import models.insee.etablissement.DisclosedStatus
import models.insee.etablissement.Etablissement
import models.insee.etablissement.Header
import models.insee.etablissement.InseeEtablissementResponse
import models.insee.etablissement.UniteLegale
import models.insee.token.InseeTokenResponse
import play.api.Logger

import java.time.OffsetDateTime
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

trait EtablissementService {}

class EtablissementServiceImpl(inseeClient: InseeClient, repository: CompanyDataRepositoryInterface)(implicit
    ec: ExecutionContext
) extends EtablissementService {

  private[this] val logger = Logger(this.getClass)

  def importEtablissement(): Future[Unit] =
    for {
      token <- inseeClient.generateToken()
      beginPeriod = OffsetDateTime.now().minusDays(10)
      firstCall <- inseeClient.getEtablissement(token, disclosedStatus = Some(DisclosedStatus.N))
      _ = logger.info(s"Company count to update  = ${firstCall.header.total}")
      _ <- iterateThroughEtablissement(beginPeriod, token, firstCall.header).recoverWith { case e =>
        logger.error("Unknown error on import", e)
        Future.failed(e)
      }
    } yield ()

  private def iterateThroughEtablissement(
      beginPeriod: OffsetDateTime,
      token: InseeTokenResponse,
      header: Header
  ): Future[InseeEtablissementResponse] =
    inseeClient
      .getEtablissement(token, None, disclosedStatus = Some(DisclosedStatus.N), cursor = header.curseurSuivant)
      .flatMap { r =>
        processEtablissements(r)
          .map { _ =>
            logger.info(s"${r.etablissements.size} companies processed")
            r
          }
          .recoverWith { case e =>
            logger.error("Error when updating etablissement database :", e)
            Future.failed(e)
          }
      }
      .flatMap { response =>
        if (header.nombre == InseeClient.EtablissementPageSize) {
          iterateThroughEtablissement(beginPeriod, token, response.header)
        } else {
          Future.successful(response)
        }
      }

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
