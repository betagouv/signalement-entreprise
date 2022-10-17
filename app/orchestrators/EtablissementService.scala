package orchestrators

import models.api.EtablissementSearchResult
import models.EtablissementData.EtablissementWithActivity
import models.SIREN
import models.SIRET
import models.insee.etablissement.DisclosedStatus
import play.api.Logger
import repositories.insee.EtablissementRepositoryInterface

import java.time.OffsetDateTime
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

  def getBySiret(sirets: List[SIRET], lastUpdated: Option[OffsetDateTime]): Future[List[EtablissementSearchResult]] =
    for {
      etablissementList <- etablissementRepository
        .searchBySirets(sirets)
      filteredList = filterUpdatedOnlyEtablissement(etablissementList, lastUpdated)
      etablissementSearchResultList = filteredList.map { case (etablissementData, maybeActivity) =>
        etablissementData.toSearchResult(maybeActivity.map(_.label), filterAdress = false)
      }
    } yield etablissementSearchResultList

  private def filterUpdatedOnlyEtablissement(
      etablissementList: List[EtablissementWithActivity],
      lastUpdated: Option[OffsetDateTime]
  ): List[EtablissementWithActivity] =
    etablissementList.filter {
      case (etablissement, _) if etablissement.statutDiffusionEtablissement == DisclosedStatus.NonPublic => true
      case (etablissement, _) =>
        val maybeEtablissementLastUpdated: Option[OffsetDateTime] =
          toOffsetDateTime(etablissement.dateDernierTraitementEtablissement)
        etablissementHasChangedAfterLastUpdated(maybeEtablissementLastUpdated, lastUpdated)
    }

  private def etablissementHasChangedAfterLastUpdated(
      maybeEtablissementLastUpdated: Option[OffsetDateTime],
      maybeFilterLastUpdated: Option[OffsetDateTime]
  ): Boolean =
    (maybeEtablissementLastUpdated, maybeFilterLastUpdated) match {
      case (Some(etablissementLastUpdated), Some(filterLastUpdated))
          if etablissementLastUpdated.isAfter(filterLastUpdated) =>
        true
      case (_, None) => true
      case _         => false
    }

}
