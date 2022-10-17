package repositories.insee

import models.ActivityCode
import models.SIREN
import models.SIRET
import models.EtablissementData

import scala.concurrent.Future

trait EtablissementRepositoryInterface {

  def insertOrUpdate(companies: Map[String, Option[String]]): Future[Int]

  def search(q: String, postalCode: String): Future[List[(EtablissementData, Option[ActivityCode])]]

  def searchBySirets(
      sirets: List[SIRET]
  ): Future[List[(EtablissementData, Option[ActivityCode])]]

  def searchBySiretIncludingHeadOfficeWithActivity(
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

}
