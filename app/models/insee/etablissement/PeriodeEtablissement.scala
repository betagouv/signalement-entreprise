package models.insee.etablissement

import play.api.libs.json.Json
import play.api.libs.json.OFormat

case class PeriodeEtablissement(
    dateDebut: Option[String],
    etatAdministratifEtablissement: Option[String],
    enseigne1Etablissement: Option[String],
    enseigne2Etablissement: Option[String],
    enseigne3Etablissement: Option[String],
    denominationUsuelleEtablissement: Option[String],
    activitePrincipaleEtablissement: Option[String],
    nomenclatureActivitePrincipaleEtablissement: Option[String],
    caractereEmployeurEtablissement: Option[String],
    dateFin: Option[String]
)

object PeriodeEtablissement {
  implicit val format: OFormat[PeriodeEtablissement] = Json.format[PeriodeEtablissement]

  def withNonEmpty(periodeEtablissement: PeriodeEtablissement) =
    PeriodeEtablissement(
      dateDebut = periodeEtablissement.dateDebut.withTrimmedNonEmpty,
      etatAdministratifEtablissement = periodeEtablissement.etatAdministratifEtablissement.withTrimmedNonEmpty,
      enseigne1Etablissement = periodeEtablissement.enseigne1Etablissement.withTrimmedNonEmpty,
      enseigne2Etablissement = periodeEtablissement.enseigne2Etablissement.withTrimmedNonEmpty,
      enseigne3Etablissement = periodeEtablissement.enseigne3Etablissement.withTrimmedNonEmpty,
      denominationUsuelleEtablissement = periodeEtablissement.denominationUsuelleEtablissement.withTrimmedNonEmpty,
      activitePrincipaleEtablissement = periodeEtablissement.activitePrincipaleEtablissement.withTrimmedNonEmpty,
      nomenclatureActivitePrincipaleEtablissement =
        periodeEtablissement.nomenclatureActivitePrincipaleEtablissement.withTrimmedNonEmpty,
      caractereEmployeurEtablissement = periodeEtablissement.caractereEmployeurEtablissement.withTrimmedNonEmpty,
      dateFin = periodeEtablissement.dateFin.withTrimmedNonEmpty
    )

}
