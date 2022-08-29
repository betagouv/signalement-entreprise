package models

import play.api.libs.json.Json
import play.api.libs.json.OFormat

case class CompanyActivity(
    code: String,
    label: String
)
object CompanyActivity {
  implicit val format: OFormat[CompanyActivity] = Json.format[CompanyActivity]
}
