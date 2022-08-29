package models.insee.etablissement

import play.api.libs.json.Json
import play.api.libs.json.OFormat

case class AdresseEtablissement(
    complementAdresseEtablissement: Option[String],
    numeroVoieEtablissement: Option[String],
    indiceRepetitionEtablissement: Option[String],
    typeVoieEtablissement: Option[String],
    libelleVoieEtablissement: Option[String],
    codePostalEtablissement: Option[String],
    libelleCommuneEtablissement: Option[String],
    libelleCommuneEtrangerEtablissement: Option[String],
    distributionSpecialeEtablissement: Option[String],
    codeCommuneEtablissement: Option[String],
    codeCedexEtablissement: Option[String],
    libelleCedexEtablissement: Option[String],
    codePaysEtrangerEtablissement: Option[String],
    libellePaysEtrangerEtablissement: Option[String]
)

object AdresseEtablissement {
  implicit val format: OFormat[AdresseEtablissement] = Json.format[AdresseEtablissement]

  def withNonEmpty(obj: AdresseEtablissement) =
    AdresseEtablissement(
      complementAdresseEtablissement = obj.complementAdresseEtablissement.withTrimmedNonEmpty,
      numeroVoieEtablissement = obj.numeroVoieEtablissement.withTrimmedNonEmpty,
      indiceRepetitionEtablissement = obj.indiceRepetitionEtablissement.withTrimmedNonEmpty,
      typeVoieEtablissement = obj.typeVoieEtablissement.withTrimmedNonEmpty,
      libelleVoieEtablissement = obj.libelleVoieEtablissement.withTrimmedNonEmpty,
      codePostalEtablissement = obj.codePostalEtablissement.withTrimmedNonEmpty,
      libelleCommuneEtablissement = obj.libelleCommuneEtablissement.withTrimmedNonEmpty,
      libelleCommuneEtrangerEtablissement = obj.libelleCommuneEtrangerEtablissement.withTrimmedNonEmpty,
      distributionSpecialeEtablissement = obj.distributionSpecialeEtablissement.withTrimmedNonEmpty,
      codeCommuneEtablissement = obj.codeCommuneEtablissement.withTrimmedNonEmpty,
      codeCedexEtablissement = obj.codeCedexEtablissement.withTrimmedNonEmpty,
      libelleCedexEtablissement = obj.libelleCedexEtablissement.withTrimmedNonEmpty,
      codePaysEtrangerEtablissement = obj.codePaysEtrangerEtablissement.withTrimmedNonEmpty,
      libellePaysEtrangerEtablissement = obj.libellePaysEtrangerEtablissement.withTrimmedNonEmpty
    )

}
