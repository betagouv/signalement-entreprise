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
import java.util.Locale
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class EtablissementService(
    val etablissementRepository: EtablissementRepositoryInterface
)(implicit ec: ExecutionContext) {

  val logger: Logger = Logger(this.getClass)

  private[orchestrators] def extractActivityLabel(activityCode: ActivityCode, lang: Option[Locale]): String =
    lang match {
      case Some(Locale.ENGLISH) => activityCode.enLabel
      case _                    => activityCode.label
    }

  def searchEtablissement(
      q: String,
      postalCode: String,
      lang: Option[Locale]
  ): Future[List[EtablissementSearchResult]] = {
    logger.info(s"searchEtablissement $postalCode $q")
    etablissementRepository
      .search(q, postalCode)
      .map(results => results.map(result => result._1.toSearchResult(result._2.map(extractActivityLabel(_, lang)))))
  }

  def searchEtablissementByIdentity(
      identity: String,
      openOnly: Option[Boolean],
      lang: Option[Locale]
  ): Future[List[EtablissementSearchResult]] = {
    val openCompaniesOnly = openOnly.getOrElse(true)
    logger.debug(s"searchEtablissementByIdentity $identity")
    for {
      etablissementsWithActivity <- extractIdentity(identity) match {
        case Some(Right(siret)) =>
          etablissementRepository.searchBySiretWithHeadOffice(siret, openCompaniesOnly)
        case Some(Left(siren)) => searchEtablissementBySiren(siren, openCompaniesOnly, onlyHeadOffice = true)
        case None              => Future.successful(List.empty)
      }
      searchResult = etablissementsWithActivity.map { case (company, activity) =>
        company.toSearchResult(activity.map(extractActivityLabel(_, lang)))
      }
    } yield searchResult
  }

  def extractIdentity(identity: String): Option[Either[SIREN, SIRET]] = (SIRET(identity), SIREN(identity)) match {
    case (Some(siret), _)    => Some(Right(siret))
    case (None, Some(siren)) => Some(Left(siren))
    case _                   => None
  }

  def getBySiren(
      sirens: List[SIREN],
      lang: Option[Locale],
      onlyHeadOffice: Option[Boolean]
  ): Future[List[EtablissementSearchResult]] =
    Future
      .sequence(
        sirens.map(
          searchEtablissementBySiren(_, openCompaniesOnly = false, onlyHeadOffice = onlyHeadOffice.getOrElse(true))
        )
      )
      .map(_.flatten)
      .map(_.map { case (company, activity) => company.toSearchResult(activity.map(extractActivityLabel(_, lang))) })

  def searchEtablissementBySiren(
      siren: SIREN,
      openCompaniesOnly: Boolean,
      onlyHeadOffice: Boolean
  ): Future[List[(EtablissementData, Option[ActivityCode])]] =
    if (onlyHeadOffice) {
      for {
        maybeHeadOffice <- etablissementRepository.searchHeadOfficeBySiren(siren, openCompaniesOnly)
        etablissements <- maybeHeadOffice match {
          case Some(openHeadOffice) if openHeadOffice._1.isOpen => Future.successful(List(openHeadOffice))
          case _ => etablissementRepository.searchBySiren(siren, openCompaniesOnly)
        }

      } yield etablissements
    } else {
      etablissementRepository.searchBySiren(siren, openCompaniesOnly)
    }

  def getBySiret(
      sirets: List[SIRET],
      lastUpdated: Option[OffsetDateTime],
      lang: Option[Locale]
  ): Future[List[EtablissementSearchResult]] =
    for {
      etablissementList <- etablissementRepository
        .searchBySirets(sirets)
      filteredList = filterUpdatedOnlyEtablissement(etablissementList, lastUpdated)
      etablissementSearchResultList = filteredList.map { case (etablissementData, maybeActivity) =>
        etablissementData.toSearchResult(maybeActivity.map(extractActivityLabel(_, lang)), filterAdress = false)
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
