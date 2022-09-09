package repositories.insee

import models.ActivityCode
import models.SIREN
import models.SIRET
import models.EtablissementData
import repositories.CRUDRepositoryInterface

import scala.concurrent.Future

trait EtablissementRepositoryInterface extends CRUDRepositoryInterface[EtablissementData] {

  def insertOrUpdate(companies: Map[String, Option[String]]): Future[Int]

  def search(q: String, postalCode: String): Future[List[(EtablissementData, Option[ActivityCode])]]

  def searchBySirets(
      sirets: List[SIRET],
      includeClosed: Boolean = false
  ): Future[List[(EtablissementData, Option[ActivityCode])]]

  def searchBySiretIncludingHeadOfficeWithActivity(
      siret: SIRET
  ): Future[List[(EtablissementData, Option[ActivityCode])]]

  def searchBySiren(
      siren: SIREN
  ): Future[List[(EtablissementData, Option[ActivityCode])]]

  def searchHeadOfficeBySiren(siren: SIREN): Future[Option[(EtablissementData, Option[ActivityCode])]]

}