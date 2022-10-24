package orchestrators

import models.ActivityCode
import models.EtablissementData
import models.SIREN
import models.SIRET
import repositories.insee.EtablissementRepositoryInterface

import scala.concurrent.Future

class EtablissementRepositoryInterfaceMock(
    searchBySiretsFunc: List[(EtablissementData, Option[ActivityCode])] = List.empty,
    searchBySiretWithHeadOfficeFunc: List[(EtablissementData, Option[ActivityCode])] = List.empty,
    searchBySirenFunc: List[(EtablissementData, Option[ActivityCode])] = List.empty,
    searchHeadOfficeBySirenFunc: Option[(EtablissementData, Option[ActivityCode])] = None
) extends EtablissementRepositoryInterface {

  override def insertOrUpdate(companies: Map[String, Option[String]]): Future[Int] = ???

  override def search(q: String, postalCode: String): Future[List[(EtablissementData, Option[ActivityCode])]] = ???

  override def searchBySirets(sirets: List[SIRET]): Future[List[(EtablissementData, Option[ActivityCode])]] =
    Future.successful(searchBySiretsFunc)

  override def searchBySiretWithHeadOffice(
      siret: SIRET,
      openCompaniesOnly: Boolean
  ): Future[List[(EtablissementData, Option[ActivityCode])]] = Future.successful(searchBySiretWithHeadOfficeFunc)

  override def searchBySiren(
      siren: SIREN,
      openCompaniesOnly: Boolean
  ): Future[List[(EtablissementData, Option[ActivityCode])]] = Future.successful(searchBySirenFunc)

  override def searchHeadOfficeBySiren(
      siren: SIREN,
      openCompaniesOnly: Boolean
  ): Future[Option[(EtablissementData, Option[ActivityCode])]] = Future.successful(searchHeadOfficeBySirenFunc)
}
