package models

import play.api.libs.json.Json
import play.api.libs.json.OFormat

// There are a bunch of other fields we don't care about
case class GeoApiCommune(
    // le code commune INSEE
    code: String,
    codeDepartement: String
)

object GeoApiCommune {
  implicit val format: OFormat[GeoApiCommune] = Json.format[GeoApiCommune]
}
