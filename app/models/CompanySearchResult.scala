package models

import play.api.libs.json.Json
import play.api.libs.json.OFormat
import utils.SIRET

case class CompanySearchResult(
    siret: SIRET,
    name: Option[String],
    brand: Option[String],
    isHeadOffice: Boolean,
    address: Address,
    activityCode: Option[String],
    activityLabel: Option[String],
    isMarketPlace: Boolean = false,
    isOpen: Boolean
)

object CompanySearchResult {
  implicit val format: OFormat[CompanySearchResult] = Json.format[CompanySearchResult]
}
