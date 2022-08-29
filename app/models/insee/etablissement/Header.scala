package models.insee.etablissement

import play.api.libs.json.Json
import play.api.libs.json.OFormat

case class Header(
    statut: Int,
    message: String,
    total: Int,
    debut: Int,
    nombre: Int,
    curseur: Option[String],
    curseurSuivant: Option[String]
)

object Header {
  implicit val HeaderFormat: OFormat[Header] = Json.format[Header]
}
