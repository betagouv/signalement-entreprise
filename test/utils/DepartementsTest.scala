package utils

import models.SIREN
import models.SIRET
import models.api.GeoApiCommune
import models.insee.etablissement.AdresseEtablissement
import models.insee.etablissement.InseeEtablissement
import models.insee.etablissement.UniteLegale
import org.specs2.mutable.Specification

class DepartementsTest extends Specification {

  val toulon = GeoApiCommune(
    code = "83137",
    codeDepartement = "83",
    codesPostaux = Seq("83000", "83100", "83200")
  )
  val tournefeuille =
    GeoApiCommune(
      code = "31557",
      codeDepartement = "31",
      codesPostaux = Seq("31170")
    )
  val lyon =
    GeoApiCommune(
      code = "69123",
      codeDepartement = "69",
      codesPostaux = Seq("69001", "69002", "69003", "69004", "69005", "69006", "69007", "69008", "69009")
    )
  val lyon7thArrondissement = GeoApiCommune(
    code = "69387",
    codeDepartement = "69",
    codesPostaux = Seq("69007")
  )
  // Châtres-la-Forêt has been absorbed by Évron
  val evron = GeoApiCommune(
    code = "53097",
    codeDepartement = "53",
    codesPostaux = Seq("53150", "53600")
  )
  val chatresLaForet = GeoApiCommune(
    code = "53065",
    codeDepartement = "53",
    codesPostaux = Seq("53600")
  )

  // this list comes from the geo api
  // it does not contain arrondissements, nor former communes
  val allCommunes = Seq(
    toulon,
    tournefeuille,
    lyon,
    evron
    // ChatresLaForet doesn't exist anymore (absorbed by Evron) thus it will NOT be in the list from the API
    // Same thing for lyon 7th arrondissement : it's not really a commune in itself, so it's not in the API
  )

  "Departements" >> {
    "find code departement in a basic commune, without relying on code postal" >> {
      val etab = buildEtablissement(
        codeCommuneEtablissement = toulon.code,
        codePostalEtablissement = None
      )
      val res = Departments.findCodeDepartementOfEtablissement(etab, allCommunes)
      res must beSome(toulon.codeDepartement)
    }
    "find code departement in another basic commune, without relying on code postal" >> {
      val etab = buildEtablissement(
        codeCommuneEtablissement = tournefeuille.code,
        codePostalEtablissement = None
      )
      val res = Departments.findCodeDepartementOfEtablissement(etab, allCommunes)
      res must beSome(tournefeuille.codeDepartement)
    }
    "find code departement for an arrondissement of Lyon" >> {
      val etab = buildEtablissement(
        codeCommuneEtablissement = lyon7thArrondissement.code,
        codePostalEtablissement = Some(lyon7thArrondissement.codesPostaux.head)
      )
      val res = Departments.findCodeDepartementOfEtablissement(etab, allCommunes)
      res must beSome(lyon7thArrondissement.codeDepartement)
    }
    "find code departement for a former commune (Châtres-la-Forêt) " >> {
      val etab = buildEtablissement(
        codeCommuneEtablissement = chatresLaForet.code,
        codePostalEtablissement = Some(chatresLaForet.codesPostaux.head)
      )
      val res = Departments.findCodeDepartementOfEtablissement(etab, allCommunes)
      res must beSome(chatresLaForet.codeDepartement)
    }

  }

  def buildEtablissement(codeCommuneEtablissement: String, codePostalEtablissement: Option[String]) =
    InseeEtablissement(
      siret = SIRET.fromUnsafe("12345678900001"),
      siren = SIREN.fromUnsafe("794996827"),
      nic = "",
      statutDiffusionEtablissement = "",
      dateCreationEtablissement = None,
      trancheEffectifsEtablissement = None,
      anneeEffectifsEtablissement = None,
      activitePrincipaleRegistreMetiersEtablissement = None,
      dateDernierTraitementEtablissement = None,
      etablissementSiege = false,
      nombrePeriodesEtablissement = 0,
      uniteLegale = UniteLegale(
        denominationUniteLegale = None,
        denominationUsuelle1UniteLegale = None,
        denominationUsuelle2UniteLegale = None,
        denominationUsuelle3UniteLegale = None,
        prenomUsuelUniteLegale = None,
        nomUsageUniteLegale = None,
        nomUniteLegale = None
      ),
      adresseEtablissement = AdresseEtablissement(
        complementAdresseEtablissement = None,
        numeroVoieEtablissement = None,
        indiceRepetitionEtablissement = None,
        typeVoieEtablissement = None,
        libelleVoieEtablissement = None,
        codePostalEtablissement = codePostalEtablissement,
        libelleCommuneEtablissement = None,
        libelleCommuneEtrangerEtablissement = None,
        distributionSpecialeEtablissement = None,
        codeCommuneEtablissement = Some(codeCommuneEtablissement),
        codeCedexEtablissement = None,
        libelleCedexEtablissement = None,
        codePaysEtrangerEtablissement = None,
        libellePaysEtrangerEtablissement = None
      ),
      adresse2Etablissement = None,
      periodesEtablissement = Nil
    )

}
