package models

import play.api.libs.json.Json
import play.api.libs.json.OFormat

case class ActivityCode(
    code: String,
    label: String,
    enLabel: String
)
object ActivityCode {
  implicit val format: OFormat[ActivityCode] = Json.format[ActivityCode]
}
