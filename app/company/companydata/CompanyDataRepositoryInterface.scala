package company.companydata

import models.CompanyActivity
import models.CompanyData
import repositories.CRUDRepositoryInterface
import utils.SIREN
import utils.SIRET

import scala.concurrent.Future

trait CompanyDataRepositoryInterface extends CRUDRepositoryInterface[CompanyData] {

  def insertOrUpdate(companies: Map[String, Option[String]]): Future[Int]

  def search(q: String, postalCode: String): Future[List[(CompanyData, Option[CompanyActivity])]]

  def searchBySirets(
      sirets: List[SIRET],
      includeClosed: Boolean = false
  ): Future[List[(CompanyData, Option[CompanyActivity])]]

  def searchBySiretIncludingHeadOfficeWithActivity(siret: SIRET): Future[List[(CompanyData, Option[CompanyActivity])]]

  def searchBySiren(
      siren: SIREN
  ): Future[List[(CompanyData, Option[CompanyActivity])]]

  def searchHeadOfficeBySiren(siren: SIREN): Future[Option[(CompanyData, Option[CompanyActivity])]]

}
