package repositories.insee

import models.ActivityCode
import models.SIREN
import models.SIRET
import models.EtablissementData

import scala.concurrent.Future

trait EtablissementRepositoryInterface {

  def insertOrUpdate(companies: Map[String, Option[String]]): Future[Int]

  def updateDepartment(siret: SIRET, codeDepartment: String): Future[Int]

  def search(
      q: String,
      postalCode: Option[String],
      departmentCode: Option[String],
      headOffice: Option[Boolean]
  ): Future[List[(EtablissementData, Option[ActivityCode])]]

  def searchBySirets(
      sirets: List[SIRET]
  ): Future[List[(EtablissementData, Option[ActivityCode])]]

  def searchBySiretWithHeadOffice(
      siret: SIRET,
      openCompaniesOnly: Boolean
  ): Future[List[(EtablissementData, Option[ActivityCode])]]

  def searchBySiren(
      siren: SIREN,
      openCompaniesOnly: Boolean
  ): Future[List[(EtablissementData, Option[ActivityCode])]]

  def searchHeadOfficeBySiren(
      siren: SIREN,
      openCompaniesOnly: Boolean
  ): Future[Option[(EtablissementData, Option[ActivityCode])]]

  def listWithoutMissingDepartment(limit: Int): Future[Seq[EtablissementData]]
}
