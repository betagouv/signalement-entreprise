package orchestrators

import models.ActivityCode
import models.EtablissementData
import models.SIREN
import models.SIRET
import models.insee.etablissement.DisclosedStatus

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import scala.concurrent.duration.Duration
import scala.concurrent.Await
import scala.concurrent.ExecutionContext

class EtablissementServiceTest extends org.specs2.mutable.Specification {

  "EtablissementService" should {

    val now        = OffsetDateTime.now()
    val futureDate = Some(now.plusDays(10).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
    val pastDate   = Some(now.minusDays(10).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))

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
        service.getBySiret(List.empty, Some(now), None),
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
        service.getBySiret(List.empty, None, None),
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
        service.searchEtablissementByIdentity(identity, openOnly = None, None),
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
        service.searchEtablissementByIdentity(siren.value, openOnly = None, None),
        Duration.Inf
      )
      res.map(_.siret) shouldEqual List(siret)
    }

    "return empty list from search on open only company given siren identity when there is only a closed headoffice " in {

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
        service.searchEtablissementByIdentity(siren.value, openOnly = Some(true), None),
        Duration.Inf
      )
      res.map(_.siret) shouldEqual List.empty
    }

    "return  list from search when no headOffice found " in {

      val siret = SIRET.fromUnsafe("11111111111111")
      val siren = SIREN.apply(siret)

      val etablissement =
        genEtablissement(
          siret,
          DisclosedStatus.Public,
          pastDate,
          isHeadOffice = Some("false"),
          isOpen = Some(EtablissementData.Closed)
        )

      val searchBySiren = List(etablissement)

      val env = genEnv(searchBySirenFunc = searchBySiren)

      import env._
      val res = Await.result(
        service.searchEtablissementByIdentity(siren.value, openOnly = Some(true), None),
        Duration.Inf
      )
      res.map(_.siret) shouldEqual List(siret)
    }

    "extract default french activity code" in {
      val activityCode = ActivityCode("code", "french", "english")

      val env = genEnv()
      import env._

      val result = service.extractActivityLabel(activityCode, None)

      result shouldEqual "french"
    }

    "fallback on french activity code" in {
      val activityCode = ActivityCode("code", "french", "english")

      val env = genEnv()
      import env._

      val result = service.extractActivityLabel(activityCode, Some(Locale.CHINESE))

      result shouldEqual "french"
    }

    "extract english activity code" in {
      val activityCode = ActivityCode("code", "french", "english")

      val env = genEnv()
      import env._

      val result = service.extractActivityLabel(activityCode, Some(Locale.forLanguageTag("en")))

      result shouldEqual "english"
    }

    "getBySiren should return only head office when parameter set to true and head office exists" in {
      val headOfficeSiret = SIRET.fromUnsafe("11111111111111")
      val siret           = SIRET.fromUnsafe("11111111111112")
      val siren           = SIREN.apply(headOfficeSiret)

      val headOffice = genEtablissement(headOfficeSiret, DisclosedStatus.Public, pastDate, isHeadOffice = Some("true"))
      val etablissement = genEtablissement(siret, DisclosedStatus.Public, pastDate, isHeadOffice = Some("false"))

      val searchHeadOfficeBySiren = Some(headOffice)
      val searchBySiren           = List(headOffice, etablissement)

      val env = genEnv(searchHeadOfficeBySiren = searchHeadOfficeBySiren, searchBySirenFunc = searchBySiren)

      import env._
      val res = Await.result(
        service.getBySiren(List(siren), lang = None, onlyHeadOffice = Some(true)),
        Duration.Inf
      )
      res.map(_.siret) shouldEqual List(headOfficeSiret)
    }

    "getBySiren should return only head office when parameter unset and head office exists" in {
      val headOfficeSiret = SIRET.fromUnsafe("11111111111111")
      val siret           = SIRET.fromUnsafe("11111111111112")
      val siren           = SIREN.apply(headOfficeSiret)

      val headOffice = genEtablissement(headOfficeSiret, DisclosedStatus.Public, pastDate, isHeadOffice = Some("true"))
      val etablissement = genEtablissement(siret, DisclosedStatus.Public, pastDate, isHeadOffice = Some("false"))

      val searchHeadOfficeBySiren = Some(headOffice)
      val searchBySiren           = List(headOffice, etablissement)

      val env = genEnv(searchHeadOfficeBySiren = searchHeadOfficeBySiren, searchBySirenFunc = searchBySiren)

      import env._
      val res = Await.result(
        service.getBySiren(List(siren), lang = None, onlyHeadOffice = None),
        Duration.Inf
      )
      res.map(_.siret) shouldEqual List(headOfficeSiret)
    }

    "getBySiren should return all companies when parameter set to true but head office does not exist" in {
      val siret = SIRET.fromUnsafe("11111111111111")
      val siren = SIREN.apply(siret)

      val etablissement =
        genEtablissement(siret, DisclosedStatus.Public, pastDate, isHeadOffice = Some("false"))

      val searchBySiren = List(etablissement)

      val env = genEnv(searchBySirenFunc = searchBySiren)

      import env._
      val res = Await.result(
        service.getBySiren(List(siren), lang = None, onlyHeadOffice = Some(true)),
        Duration.Inf
      )
      res.map(_.siret) shouldEqual List(siret)
    }

    "getBySiren should return all companies when set to false" in {
      val headOfficeSiret = SIRET.fromUnsafe("11111111111111")
      val siret           = SIRET.fromUnsafe("11111111111112")
      val siren           = SIREN.apply(headOfficeSiret)

      val headOffice = genEtablissement(headOfficeSiret, DisclosedStatus.Public, pastDate, isHeadOffice = Some("true"))
      val etablissement = genEtablissement(siret, DisclosedStatus.Public, pastDate, isHeadOffice = Some("false"))

      val searchBySiren = List(headOffice, etablissement)

      val env = genEnv(searchBySirenFunc = searchBySiren)

      import env._
      val res = Await.result(
        service.getBySiren(List(siren), lang = None, onlyHeadOffice = Some(false)),
        Duration.Inf
      )
      res.map(_.siret) shouldEqual List(headOfficeSiret, siret)
    }

  }

  def genEnv(
      searchBySiretsFunc: List[(EtablissementData, Option[ActivityCode])] = List.empty,
      searchBySirenFunc: List[(EtablissementData, Option[ActivityCode])] = List.empty,
      searchBySiretWithHeadOffice: List[(EtablissementData, Option[ActivityCode])] = List.empty,
      searchHeadOfficeBySiren: Option[(EtablissementData, Option[ActivityCode])] = None
  ) = new {

    implicit val ec: ExecutionContext = ExecutionContext.global

    val repo = new EtablissementRepositoryInterfaceMock(
      searchBySiretsFunc = searchBySiretsFunc,
      searchBySirenFunc = searchBySirenFunc,
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
        codePaysEtrangerEtablissement = None,
        distributionSpecialeEtablissement = None,
        codeCommuneEtablissement = None,
        codeCedexEtablissement = None,
        libelleCedexEtablissement = None,
        denomination = None,
        denominationUsuelle1UniteLegale = None,
        denominationUsuelle2UniteLegale = None,
        denominationUsuelle3UniteLegale = None,
        enseigne1Etablissement = None,
        enseigne2Etablissement = None,
        enseigne3Etablissement = None,
        activitePrincipaleEtablissement = None,
        etatAdministratifEtablissement = isOpen,
        statutDiffusionEtablissement = disclosedStatus,
        nomCommercialEtablissement = None,
        codeDepartement = None
      ),
      None
    )

}
