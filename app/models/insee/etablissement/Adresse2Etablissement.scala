package models.insee.etablissement

import play.api.libs.json.Json
import play.api.libs.json.OFormat

case class Adresse2Etablissement(
    complementAdresse2Etablissement: Option[String],
    numeroVoie2Etablissement: Option[String],
    indiceRepetition2Etablissement: Option[String],
    typeVoie2Etablissement: Option[String],
    libelleVoie2Etablissement: Option[String],
    codePostal2Etablissement: Option[String],
    libelleCommune2Etablissement: Option[String],
    libelleCommuneEtranger2Etablissement: Option[String],
    distributionSpeciale2Etablissement: Option[String],
    codeCommune2Etablissement: Option[String],
    codeCedex2Etablissement: Option[String],
    libelleCedex2Etablissement: Option[String],
    codePaysEtranger2Etablissement: Option[String],
    libellePaysEtranger2Etablissement: Option[String]
)

object Adresse2Etablissement {
  implicit val format: OFormat[Adresse2Etablissement] = Json.format[Adresse2Etablissement]

  def withNonEmpty(obj: Adresse2Etablissement) =
    Adresse2Etablissement(
      complementAdresse2Etablissement = obj.complementAdresse2Etablissement.withTrimmedNonEmpty,
      numeroVoie2Etablissement = obj.numeroVoie2Etablissement.withTrimmedNonEmpty,
      indiceRepetition2Etablissement = obj.indiceRepetition2Etablissement.withTrimmedNonEmpty,
      typeVoie2Etablissement = obj.typeVoie2Etablissement.withTrimmedNonEmpty,
      libelleVoie2Etablissement = obj.libelleVoie2Etablissement.withTrimmedNonEmpty,
      codePostal2Etablissement = obj.codePostal2Etablissement.withTrimmedNonEmpty,
      libelleCommune2Etablissement = obj.libelleCommune2Etablissement.withTrimmedNonEmpty,
      libelleCommuneEtranger2Etablissement = obj.libelleCommuneEtranger2Etablissement.withTrimmedNonEmpty,
      distributionSpeciale2Etablissement = obj.distributionSpeciale2Etablissement.withTrimmedNonEmpty,
      codeCommune2Etablissement = obj.codeCommune2Etablissement.withTrimmedNonEmpty,
      codeCedex2Etablissement = obj.codeCedex2Etablissement.withTrimmedNonEmpty,
      libelleCedex2Etablissement = obj.libelleCedex2Etablissement.withTrimmedNonEmpty,
      codePaysEtranger2Etablissement = obj.codePaysEtranger2Etablissement.withTrimmedNonEmpty,
      libellePaysEtranger2Etablissement = obj.libellePaysEtranger2Etablissement.withTrimmedNonEmpty
    )
}
