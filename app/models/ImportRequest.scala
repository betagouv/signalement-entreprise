package models

import play.api.libs.json.Json
import play.api.libs.json.OFormat

import java.time.OffsetDateTime

case class ImportRequest(
    begin: Option[OffsetDateTime],
    end: Option[OffsetDateTime],
    siret: Option[SIRET]
)

object ImportRequest {
  implicit val format: OFormat[ImportRequest] = Json.format[ImportRequest]
}
