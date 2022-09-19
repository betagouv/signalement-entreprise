package models.insee.token

import models.insee.etablissement.DisclosedStatus

import java.time.OffsetDateTime

case class InseeEtablissementQuery(
    token: InseeTokenResponse,
    beginPeriod: Option[OffsetDateTime] = None,
    endPeriod: Option[OffsetDateTime] = None,
    disclosedStatus: Option[DisclosedStatus] = None
)
