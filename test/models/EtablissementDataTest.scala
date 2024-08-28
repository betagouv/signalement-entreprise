package models

import models.insee.etablissement.DisclosedStatus

import org.specs2.mutable.Specification

class EtablissementDataTest extends Specification {

  "EtablissementData" should {

    "return None when all fields are None" in {
      val data = EtablissementData(
        siret = SIRET.fromUnsafe("12345678901234"),
        siren = SIREN.fromUnsafe("123456789"),
        dateDernierTraitementEtablissement = None,
        etablissementSiege = None,
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
        nomCommercialEtablissement = None,
        enseigne1Etablissement = None,
        enseigne2Etablissement = None,
        enseigne3Etablissement = None,
        activitePrincipaleEtablissement = None,
        etatAdministratifEtablissement = None,
        statutDiffusionEtablissement = DisclosedStatus.NonPublicLegacy,
        codeDepartement = None
      )
      data.computeCommercialName must beNone
    }

    "return None when all fields are blank" in {
      val data = EtablissementData(
        siret = SIRET.fromUnsafe("12345678901234"),
        siren = SIREN.fromUnsafe("123456789"),
        dateDernierTraitementEtablissement = None,
        etablissementSiege = None,
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
        denominationUsuelle1UniteLegale = Some(""),
        denominationUsuelle2UniteLegale = Some(" "),
        denominationUsuelle3UniteLegale = Some("   "),
        nomCommercialEtablissement = None,
        enseigne1Etablissement = Some(""),
        enseigne2Etablissement = Some(" "),
        enseigne3Etablissement = Some("   "),
        activitePrincipaleEtablissement = None,
        etatAdministratifEtablissement = None,
        statutDiffusionEtablissement = DisclosedStatus.NonPublicLegacy,
        codeDepartement = Some("  ")
      )
      data.computeCommercialName must beNone
      data.computeEnseigne must beNone
    }

    "return the correct commercial name and enseigne when all fields are non-empty" in {
      val data = EtablissementData(
        siret = SIRET.fromUnsafe("12345678901234"),
        siren = SIREN.fromUnsafe("123456789"),
        dateDernierTraitementEtablissement = None,
        etablissementSiege = None,
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
        denominationUsuelle1UniteLegale = Some("Name1"),
        denominationUsuelle2UniteLegale = Some("Name2"),
        denominationUsuelle3UniteLegale = Some("Name3"),
        nomCommercialEtablissement = None,
        enseigne1Etablissement = Some("Name4"),
        enseigne2Etablissement = Some("Name5"),
        enseigne3Etablissement = Some("Name6"),
        activitePrincipaleEtablissement = None,
        etatAdministratifEtablissement = None,
        statutDiffusionEtablissement = DisclosedStatus.NonPublicLegacy,
        codeDepartement = Some("75")
      )
      data.computeCommercialName must beSome("Name1 - Name2 - Name3")
      data.computeEnseigne must beSome("Name4 - Name5 - Name6")
    }

    "return the correct commercial name and enseigne when some fields are empty" in {
      val data = EtablissementData(
        siret = SIRET.fromUnsafe("12345678901234"),
        siren = SIREN.fromUnsafe("123456789"),
        dateDernierTraitementEtablissement = None,
        etablissementSiege = None,
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
        denominationUsuelle1UniteLegale = Some("Name1"),
        denominationUsuelle2UniteLegale = None,
        denominationUsuelle3UniteLegale = Some("Name3"),
        nomCommercialEtablissement = None,
        enseigne1Etablissement = Some("Name4"),
        enseigne2Etablissement = None,
        enseigne3Etablissement = Some("Name5"),
        activitePrincipaleEtablissement = None,
        etatAdministratifEtablissement = None,
        statutDiffusionEtablissement = DisclosedStatus.NonPublicLegacy,
        codeDepartement = Some("75")
      )
      data.computeCommercialName must beSome("Name1 - Name3")
      data.computeEnseigne must beSome("Name4 - Name5")
    }

    "return the correct commercial name and enseigne when some fields are blank" in {
      val data = EtablissementData(
        siret = SIRET.fromUnsafe("12345678901234"),
        siren = SIREN.fromUnsafe("123456789"),
        dateDernierTraitementEtablissement = None,
        etablissementSiege = None,
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
        denominationUsuelle1UniteLegale = Some("Name1"),
        denominationUsuelle2UniteLegale = Some("  "),
        denominationUsuelle3UniteLegale = Some("Name3"),
        nomCommercialEtablissement = None,
        enseigne1Etablissement = Some("Name4"),
        enseigne2Etablissement = Some("  "),
        enseigne3Etablissement = Some("Name5"),
        activitePrincipaleEtablissement = None,
        etatAdministratifEtablissement = None,
        statutDiffusionEtablissement = DisclosedStatus.NonPublicLegacy,
        codeDepartement = Some("75")
      )
      data.computeCommercialName must beSome("Name1 - Name3")
      data.computeEnseigne must beSome("Name4 - Name5")
    }
  }

}
