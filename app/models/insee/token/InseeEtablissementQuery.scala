package models.insee.token

import models.SIRET
import models.insee.etablissement.DisclosedStatus

import java.time.OffsetDateTime

case class InseeEtablissementQuery(
    beginPeriod: Option[OffsetDateTime] = None,
    endPeriod: Option[OffsetDateTime] = None,
    siret: Option[SIRET] = None,
    disclosedStatus: Option[DisclosedStatus] = None
)
