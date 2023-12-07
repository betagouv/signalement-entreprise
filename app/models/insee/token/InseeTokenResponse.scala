package models.insee.token

import play.api.libs.json.JsonNaming.SnakeCase
import play.api.libs.json.Json
import play.api.libs.json.JsonConfiguration
import play.api.libs.json.JsonConfiguration.Aux
import play.api.libs.json.OFormat

case class InseeTokenResponse(accessToken: InseeAccessToken, expiresIn: Int)

object InseeTokenResponse {
  implicit val config: Aux[Json.MacroOptions]                        = JsonConfiguration(SnakeCase)
  implicit val InseeTokenResponseFormat: OFormat[InseeTokenResponse] = Json.format[InseeTokenResponse]
}
