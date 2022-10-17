package orchestrators

import models.ActivityCode
import models.EtablissementData
import models.Siren
import models.Siret
import models.insee.etablissement.DisclosedStatus
import repositories.insee.EtablissementRepositoryInterface

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import scala.concurrent.duration.Duration
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class EtablissementServiceTest extends org.specs2.mutable.Specification {

  "EtablissementService" should {

    val now = OffsetDateTime.now()
    val futureDate = Some(now.plusDays(10).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
    val pastDate = Some(now.minusDays(10).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))

    val siretNonDisclosedNewlyUpdated = genEtablissement(
      Siret.fromUnsafe("11111111111111"),
      DisclosedStatus.NonPublic,
      futureDate
    )
    val siretDisclosedNewlyUpdated =
      genEtablissement(Siret.fromUnsafe("22222222222222"), DisclosedStatus.Public, futureDate)
    val siretNonDisclosedUntouched =
      genEtablissement(Siret.fromUnsafe("33333333333333"), DisclosedStatus.NonPublic, pastDate)
    val siretDisclosedUntouched = genEtablissement(Siret.fromUnsafe("44444444444444"), DisclosedStatus.Public, pastDate)

    val searchMockedResult = List(
      siretNonDisclosedNewlyUpdated,
      siretDisclosedNewlyUpdated,
      siretNonDisclosedUntouched,
      siretDisclosedUntouched
    )

    "only filter out disclosed untouch etablissement when lastupdated filter is present" in {

      val env = genEnv(searchMockedResult)
      import env._

      val res = Await.result(
        service.getBySiret(List.empty, Some(now)),
        Duration.Inf
      )

      res.size shouldEqual 3
      res.exists(_.siret == siretNonDisclosedNewlyUpdated._1.siret) shouldEqual true
      res.exists(_.siret == siretDisclosedNewlyUpdated._1.siret) shouldEqual true
      res.exists(_.siret == siretNonDisclosedUntouched._1.siret) shouldEqual true
      res.exists(_.siret == siretDisclosedUntouched._1.siret) shouldEqual false
    }

    "return all etablissement when lastupdated filter is missing" in {

      val env = genEnv(searchMockedResult)
      import env._

      val res = Await.result(
        service.getBySiret(List.empty, None),
        Duration.Inf
      )

      res.size shouldEqual 4
      res.exists(_.siret == siretNonDisclosedNewlyUpdated._1.siret) shouldEqual true
      res.exists(_.siret == siretDisclosedNewlyUpdated._1.siret) shouldEqual true
      res.exists(_.siret == siretNonDisclosedUntouched._1.siret) shouldEqual true
      res.exists(_.siret == siretDisclosedUntouched._1.siret) shouldEqual true
    }

  }

  def genEnv(searchBySiretsFunc: List[(EtablissementData, Option[ActivityCode])]) = new {

    implicit val ec = ExecutionContext.global

    val repo = new EtablissementRepositoryInterface {

      override def insertOrUpdate(companies: Map[String, Option[String]]): Future[Int] = ???

      override def search(q: String, postalCode: String): Future[List[(EtablissementData, Option[ActivityCode])]] = ???

      override def searchBySirets(sirets: List[Siret]): Future[List[(EtablissementData, Option[ActivityCode])]] =
        Future.successful(searchBySiretsFunc)

      override def searchBySiretIncludingHeadOfficeWithActivity(
          siret: Siret,
          openCompaniesOnly: Boolean
      ): Future[List[(EtablissementData, Option[ActivityCode])]] = ???

      override def searchBySiren(
          siren: Siren,
          openCompaniesOnly: Boolean
      ): Future[List[(EtablissementData, Option[ActivityCode])]] = ???

      override def searchHeadOfficeBySiren(
          siren: Siren,
          openCompaniesOnly: Boolean
      ): Future[Option[(EtablissementData, Option[ActivityCode])]] =
        ???
    }

    val service = new EtablissementService(repo)
  }

  def genEtablissement(
      siret: Siret,
      disclosedStatus: DisclosedStatus,
      dernierTraitementEtablissement: Option[String]
  ): (EtablissementData, Option[ActivityCode]) =
    (
      new EtablissementData(
        id = UUID.randomUUID(),
        siret = siret,
        siren = Siren.fromUnsafe(siret.value.substring(0, Siren.SirenLength)),
        dateDernierTraitementEtablissement = dernierTraitementEtablissement,
        etablissementSiege = None,
        complementAdresseEtablissement = None,
        numeroVoieEtablissement = None,
        indiceRepetitionEtablissement = None,
        typeVoieEtablissement = None,
        libelleVoieEtablissement = None,
        codePostalEtablissement = None,
        libelleCommuneEtablissement = None,
        libelleCommuneEtrangerEtablissement = None,
        distributionSpecialeEtablissement = None,
        codeCommuneEtablissement = None,
        codeCedexEtablissement = None,
        libelleCedexEtablissement = None,
        denominationUsuelleEtablissement = None,
        enseigne1Etablissement = None,
        activitePrincipaleEtablissement = None,
        etatAdministratifEtablissement = None,
        statutDiffusionEtablissement = disclosedStatus
      ),
      None
    )

}
