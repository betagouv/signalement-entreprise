package orchestrators

import models.api.EtablissementSearchResult
import models.SIREN
import models.SIRET

import play.api.Logger
import repositories.insee.EtablissementRepositoryInterface

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class EtablissementService(
    val etablissementRepository: EtablissementRepositoryInterface
)(implicit ec: ExecutionContext) {

  val logger: Logger = Logger(this.getClass)

  def searchEtablissement(q: String, postalCode: String): Future[List[EtablissementSearchResult]] = {
    logger.debug(s"searchEtablissement $postalCode $q")
    etablissementRepository
      .search(q, postalCode)
      .map(results => results.map(result => result._1.toSearchResult(result._2.map(_.label))))
  }

  def searchEtablissementByIdentity(identity: String): Future[List[EtablissementSearchResult]] = {
    logger.debug(s"searchEtablissementByIdentity $identity")
    (identity.replaceAll("\\s", "") match {
      case q if q.matches(SIRET.pattern) =>
        etablissementRepository.searchBySiretIncludingHeadOfficeWithActivity(SIRET(q))
      case q =>
        SIREN.pattern.r
          .findFirstIn(q)
          .map(siren =>
            for {
              headOffice <- etablissementRepository.searchHeadOfficeBySiren(SIREN(siren))
              etablissements <- headOffice
                .map(company => Future(List(company)))
                .getOrElse(etablissementRepository.searchBySiren(SIREN(siren)))
            } yield etablissements
          )
          .getOrElse(Future(List.empty))
    }).map(etablissementsWithActivity =>
      etablissementsWithActivity.map { case (company, activity) =>
        company.toSearchResult(activity.map(_.label))
      }
    )
  }

  def getBySiret(sirets: List[SIRET]): Future[List[EtablissementSearchResult]] =
    etablissementRepository
      .searchBySirets(sirets, includeClosed = true)
      .map { etablissements =>
        println(s"------------------ etablissements = ${etablissements} ------------------")
        etablissements.map { case (etablissementData, maybeActivity) =>
          etablissementData.toSearchResult(maybeActivity.map(_.label))
        }
      }

}
