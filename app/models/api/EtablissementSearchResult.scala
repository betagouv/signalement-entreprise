package models.api

import models.SIRET
import play.api.libs.json.Json
import play.api.libs.json.OFormat

import java.time.OffsetDateTime

case class EtablissementSearchResult(
                                      siret: SIRET,
                                      name: Option[String],
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
  implicit val format: OFormat[EtablissementSearchResult] = Json.format[EtablissementSearchResult]
}
