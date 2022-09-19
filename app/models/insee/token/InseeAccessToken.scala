package models.insee.token

import play.api.libs.json.Format
import play.api.libs.json.Json

case class InseeAccessToken(value: String) extends AnyVal

object InseeAccessToken {
  implicit val format: Format[InseeAccessToken] = Json.valueFormat[InseeAccessToken]
}
