package orchestrators

import models.ActivityCode
import models.EtablissementData
import models.SIREN
import models.SIRET
import models.insee.etablissement.DisclosedStatus

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import scala.collection.immutable.List
import scala.concurrent.duration.Duration
import scala.concurrent.Await
import scala.concurrent.ExecutionContext

class EtablissementServiceTest extends org.specs2.mutable.Specification {

  "EtablissementService" should {

    val now = OffsetDateTime.now()
    val futureDate = Some(now.plusDays(10).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
    val pastDate = Some(now.minusDays(10).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))

    val siretNonDisclosedNewlyUpdated = genEtablissement(
      SIRET.fromUnsafe("11111111111111"),
      DisclosedStatus.NonPublic,
      futureDate
    )
    val siretDisclosedNewlyUpdated =
      genEtablissement(SIRET.fromUnsafe("22222222222222"), DisclosedStatus.Public, futureDate)
    val siretNonDisclosedUntouched =
      genEtablissement(SIRET.fromUnsafe("33333333333333"), DisclosedStatus.NonPublic, pastDate)
    val siretDisclosedUntouched = genEtablissement(SIRET.fromUnsafe("44444444444444"), DisclosedStatus.Public, pastDate)

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

    "search by identity when siret is given" in {

      val identity = "11111111111111"

      val etablissement =
        genEtablissement(SIRET.fromUnsafe(identity), DisclosedStatus.Public, pastDate)

      val searchBySiretWithHeadOffice = List(etablissement)

      val env = genEnv(searchBySiretWithHeadOffice = searchBySiretWithHeadOffice)

      import env._
      val res = Await.result(
        service.searchEtablissementByIdentity(identity, openOnly = None),
        Duration.Inf
      )
      res.map(_.siret) shouldEqual List(SIRET.fromUnsafe(identity))

    }

    "return headOffice from siren identity" in {

      val siret = SIRET.fromUnsafe("11111111111111")
      val siren = SIREN.apply(siret)

      val etablissement =
        genEtablissement(siret, DisclosedStatus.Public, pastDate, isHeadOffice = Some("true"))

      val searchHeadOfficeBySiren = Some(etablissement)

      val env = genEnv(searchHeadOfficeBySiren = searchHeadOfficeBySiren)

      import env._
      val res = Await.result(
        service.searchEtablissementByIdentity(siren.value, openOnly = None),
        Duration.Inf
      )
      res.map(_.siret) shouldEqual List(siret)
    }

    "return closed headOffice from siren identity when there is only a closed headoffice" in {

      val siret = SIRET.fromUnsafe("11111111111111")
      val siren = SIREN.apply(siret)

      val etablissement =
        genEtablissement(
          siret,
          DisclosedStatus.Public,
          pastDate,
          isHeadOffice = Some("true"),
          isOpen = Some(EtablissementData.Closed)
        )

      val searchHeadOfficeBySiren = Some(etablissement)

      val env = genEnv(searchHeadOfficeBySiren = searchHeadOfficeBySiren)

      import env._
      val res = Await.result(
        service.searchEtablissementByIdentity(siren.value, openOnly = Some(false)),
        Duration.Inf
      )
      res.map(_.siret) shouldEqual List(siret)
    }

    " return empty list from search on open only company given siren identity when there is only a closed headoffice " in {

      val siret = SIRET.fromUnsafe("11111111111111")
      val siren = SIREN.apply(siret)

      val etablissement =
        genEtablissement(
          siret,
          DisclosedStatus.Public,
          pastDate,
          isHeadOffice = Some("true"),
          isOpen = Some(EtablissementData.Closed)
        )

      val searchHeadOfficeBySiren = Some(etablissement)

      val env = genEnv(searchHeadOfficeBySiren = searchHeadOfficeBySiren)

      import env._
      val res = Await.result(
        service.searchEtablissementByIdentity(siren.value, openOnly = Some(true)),
        Duration.Inf
      )
      res.map(_.siret) shouldEqual List.empty
    }

    "return closed headOffice with subcompanies from siren identity" in {

      val siret = SIRET.fromUnsafe("11111111111111")
      val siren = SIREN.apply(siret)

      val etablissement =
        genEtablissement(
          siret,
          DisclosedStatus.Public,
          pastDate,
          isHeadOffice = Some("true"),
          isOpen = Some(EtablissementData.Closed)
        )

      val searchHeadOfficeBySiren = Some(etablissement)

      val env = genEnv(searchHeadOfficeBySiren = searchHeadOfficeBySiren)

      import env._
      val res = Await.result(
        service.searchEtablissementByIdentity(siren.value, openOnly = Some(false)),
        Duration.Inf
      )
      res.map(_.siret) shouldEqual List(siret)
    }

  }

  def genEnv(
      searchBySiretsFunc: List[(EtablissementData, Option[ActivityCode])] = List.empty,
      searchBySiretWithHeadOffice: List[(EtablissementData, Option[ActivityCode])] = List.empty,
      searchHeadOfficeBySiren: Option[(EtablissementData, Option[ActivityCode])] = None
  ) = new {

    implicit val ec = ExecutionContext.global

    val repo = new EtablissementRepositoryInterfaceMock(
      searchBySiretsFunc = searchBySiretsFunc,
      searchBySiretWithHeadOfficeFunc = searchBySiretWithHeadOffice,
      searchHeadOfficeBySirenFunc = searchHeadOfficeBySiren
    )

    val service = new EtablissementService(repo)
  }

  def genEtablissement(
      siret: SIRET,
      disclosedStatus: DisclosedStatus,
      dernierTraitementEtablissement: Option[String],
      isHeadOffice: Option[String] = None,
      isOpen: Option[String] = None
  ): (EtablissementData, Option[ActivityCode]) =
    (
      new EtablissementData(
        id = UUID.randomUUID(),
        siret = siret,
        siren = SIREN.fromUnsafe(siret.value.substring(0, SIREN.SirenLength)),
        dateDernierTraitementEtablissement = dernierTraitementEtablissement,
        etablissementSiege = isHeadOffice,
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
        etatAdministratifEtablissement = isOpen,
        statutDiffusionEtablissement = disclosedStatus
      ),
      None
    )

}
