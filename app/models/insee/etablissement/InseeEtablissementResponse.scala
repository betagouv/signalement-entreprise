package models.insee.etablissement

import play.api.libs.json.Json
import play.api.libs.json.OFormat

case class InseeEtablissementResponse(header: Header, etablissements: List[Etablissement])

object InseeEtablissementResponse {
  implicit val format: OFormat[InseeEtablissementResponse] = Json.format[InseeEtablissementResponse]
}
