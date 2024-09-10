package utils

import models.api.GeoApiCommune
import models.insee.etablissement.InseeEtablissement

object Departments {
  def findCodeDepartementOfEtablissement(
      etablissement: InseeEtablissement,
      allCommunes: Seq[GeoApiCommune]
  ): Option[String] = {
    val isEtranger = etablissement.adresseEtablissement.libellePaysEtrangerEtablissement.isDefined ||
      etablissement.adresseEtablissement.codePaysEtrangerEtablissement.isDefined
    if (isEtranger) {
      None
    } else {
      val codeCommune = etablissement.adresseEtablissement.codeCommuneEtablissement
      val codePostal  = etablissement.adresseEtablissement.codePostalEtablissement
      val commune = codeCommune
        .flatMap(code => allCommunes.find(_.code == code))
        // As a fallback, we use the code postal
        // This is less accurate, because in some very niche cases a code postal might be
        // used across several communes which are across several departments
        // ex : 42620
        // see https://fr.wikipedia.org/wiki/Liste_des_communes_de_France_dont_le_code_postal_ne_correspond_pas_au_d%C3%A9partement
        .orElse(codePostal.flatMap(code => allCommunes.find(_.codesPostaux.contains(code))))
      commune.map(_.codeDepartement)
    }

  }

}
