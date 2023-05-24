package orchestrators

import models.api.EtablissementSearchResult
import models.EtablissementData.EtablissementWithActivity
import models.ActivityCode
import models.EtablissementData
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

  def searchEtablissementByIdentity(
      identity: String,
      openOnly: Option[Boolean]
  ): Future[List[EtablissementSearchResult]] = {
    val openCompaniesOnly = openOnly.getOrElse(true)
    logger.debug(s"searchEtablissementByIdentity $identity")
    for {
      etablissementsWithActivity <- extractIdentity(identity) match {
        case Some(Right(siret)) =>
          etablissementRepository.searchBySiretWithHeadOffice(siret, openCompaniesOnly)
        case Some(Left(siren)) => searchEtablissementBySiren(siren, openCompaniesOnly)
        case None              => Future.successful(List.empty)
      }
      searchResult = etablissementsWithActivity.map { case (company, activity) =>
        company.toSearchResult(activity.map(_.label))
      }
    } yield searchResult
  }

  def extractIdentity(identity: String): Option[Either[SIREN, SIRET]] = (SIRET(identity), SIREN(identity)) match {
    case (Some(siret), _)    => Some(Right(siret))
    case (None, Some(siren)) => Some(Left(siren))
    case _                   => None
  }

  def searchEtablissementBySiren(
      siren: SIREN,
      openCompaniesOnly: Boolean
  ): Future[List[(EtablissementData, Option[ActivityCode])]] =
    for {
      maybeHeadOffice <- etablissementRepository.searchHeadOfficeBySiren(siren, openCompaniesOnly)
      etablissements <- maybeHeadOffice match {
        case Some(openHeadOffice) if openHeadOffice._1.isOpen => Future.successful(List(openHeadOffice))
        case _ => etablissementRepository.searchBySiren(siren, openCompaniesOnly)
      }

    } yield etablissements

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
      case (etablissement, _)
          if etablissement.statutDiffusionEtablissement == DisclosedStatus.NonPublic ||
            etablissement.statutDiffusionEtablissement == DisclosedStatus.NonPublicLegacy =>
        true
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
