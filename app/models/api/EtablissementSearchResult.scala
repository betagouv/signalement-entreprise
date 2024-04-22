package models.api

import models.SIRET
import play.api.libs.json.Json
import play.api.libs.json.OWrites

import java.time.OffsetDateTime

case class EtablissementSearchResult(
    siret: SIRET,
    name: Option[String],
    commercialName: Option[String],
    establishmentCommercialName: Option[String],
    brand: Option[String],
    isHeadOffice: Boolean,
    address: Address,
    activityCode: Option[String],
    activityLabel: Option[String],
    isMarketPlace: Boolean = false,
    isOpen: Boolean,
    isPublic: Boolean,
    lastUpdated: Option[OffsetDateTime]
)

object EtablissementSearchResult {
  implicit val writes: OWrites[EtablissementSearchResult] = Json.writes[EtablissementSearchResult]
}
