package repositories.insee

import models.ActivityCode
import models.Siren
import models.Siret
import models.EtablissementData

import scala.concurrent.Future

trait EtablissementRepositoryInterface {

  def insertOrUpdate(companies: Map[String, Option[String]]): Future[Int]

  def search(q: String, postalCode: String): Future[List[(EtablissementData, Option[ActivityCode])]]

  def searchBySirets(
      sirets: List[Siret]
  ): Future[List[(EtablissementData, Option[ActivityCode])]]

  def searchBySiretIncludingHeadOfficeWithActivity(
      siret: Siret,
      openCompaniesOnly: Boolean
  ): Future[List[(EtablissementData, Option[ActivityCode])]]

  def searchBySiren(
      siren: Siren,
      openCompaniesOnly: Boolean
  ): Future[List[(EtablissementData, Option[ActivityCode])]]

  def searchHeadOfficeBySiren(
      siren: Siren,
      openCompaniesOnly: Boolean
  ): Future[Option[(EtablissementData, Option[ActivityCode])]]

}
