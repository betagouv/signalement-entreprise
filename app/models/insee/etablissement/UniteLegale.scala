package models.insee.etablissement

import play.api.libs.json.Json
import play.api.libs.json.OFormat

case class UniteLegale(
    denominationUniteLegale: Option[String],
    denominationUsuelle1UniteLegale: Option[String],
    denominationUsuelle2UniteLegale: Option[String],
    denominationUsuelle3UniteLegale: Option[String],
    prenomUsuelUniteLegale: Option[String],
    nomUsageUniteLegale: Option[String],
    nomUniteLegale: Option[String]
)

object UniteLegale {

  implicit val format: OFormat[UniteLegale] = Json.format[UniteLegale]

  def withNonEmpty(uniteLegale: UniteLegale) =
    UniteLegale(
      denominationUniteLegale = uniteLegale.denominationUniteLegale.withTrimmedNonEmpty,
      denominationUsuelle1UniteLegale = uniteLegale.denominationUsuelle1UniteLegale.withTrimmedNonEmpty,
      denominationUsuelle2UniteLegale = uniteLegale.denominationUsuelle2UniteLegale.withTrimmedNonEmpty,
      denominationUsuelle3UniteLegale = uniteLegale.denominationUsuelle3UniteLegale.withTrimmedNonEmpty,
      prenomUsuelUniteLegale = uniteLegale.prenomUsuelUniteLegale.withTrimmedNonEmpty,
      nomUsageUniteLegale = uniteLegale.nomUsageUniteLegale.withTrimmedNonEmpty,
      nomUniteLegale = uniteLegale.nomUniteLegale.withTrimmedNonEmpty
    )

}
