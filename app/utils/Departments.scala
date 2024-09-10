package utils

import models.EtablissementData
import models.api.GeoApiCommune
import models.insee.etablissement.InseeEtablissement

object Departments {
  def findCodeDepartementOfEtablissement(
      etablissement: Either[InseeEtablissement, EtablissementData],
      allCommunes: Seq[GeoApiCommune]
  ): Option[String] = {
    val libellePaysEtranger =
      etablissement.fold(_.adresseEtablissement.libellePaysEtrangerEtablissement, _.libellePaysEtrangerEtablissement)
    val codePaysEtranger =
      etablissement.fold(_.adresseEtablissement.codePaysEtrangerEtablissement, _.codePaysEtrangerEtablissement)
    val codeCommune =
      etablissement.fold(_.adresseEtablissement.codeCommuneEtablissement, _.codeCommuneEtablissement)
    val codePostal =
      etablissement.fold(_.adresseEtablissement.codePostalEtablissement, _.codePostalEtablissement)
    val isEtranger = libellePaysEtranger.isDefined ||
      codePaysEtranger.isDefined
    if (isEtranger) {
      None
    } else {
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
