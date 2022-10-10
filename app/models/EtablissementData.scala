package models

import models.api.Address
import models.api.EtablissementSearchResult
import models.insee.etablissement.DisclosedStatus
import orchestrators.toOffsetDateTime
import play.api.libs.json.Json
import play.api.libs.json.OFormat

import java.util.UUID

case class EtablissementData(
    id: UUID = UUID.randomUUID(),
    siret: SIRET,
    siren: SIREN,
    dateDernierTraitementEtablissement: Option[String],
    etablissementSiege: Option[String],
    complementAdresseEtablissement: Option[String],
    numeroVoieEtablissement: Option[String],
    indiceRepetitionEtablissement: Option[String],
    typeVoieEtablissement: Option[String],
    libelleVoieEtablissement: Option[String],
    codePostalEtablissement: Option[String],
    libelleCommuneEtablissement: Option[String],
    libelleCommuneEtrangerEtablissement: Option[String],
    distributionSpecialeEtablissement: Option[String],
    codeCommuneEtablissement: Option[String],
    codeCedexEtablissement: Option[String],
    libelleCedexEtablissement: Option[String],
    denominationUsuelleEtablissement: Option[String],
    enseigne1Etablissement: Option[String],
    activitePrincipaleEtablissement: Option[String],
    etatAdministratifEtablissement: Option[String],
    statutDiffusionEtablissement: DisclosedStatus
) {
  def toAddress(): Address = Address(
    number = numeroVoieEtablissement,
    street = Option(
      Seq(
        typeVoieEtablissement.flatMap(typeVoie => TypeVoies.values.find(_._1 == typeVoie).map(_._2.toUpperCase)),
        libelleVoieEtablissement
      ).flatten
    )
      .filterNot(_.isEmpty)
      .map(_.mkString(" ")),
    postalCode = codePostalEtablissement,
    city = libelleCommuneEtablissement,
    addressSupplement = complementAdresseEtablissement
  )

  def toFilteredAddress(): Address = {
    val address = toAddress()
    address.copy(
      number = address.number
        .filter(_ => this.statutDiffusionEtablissement == DisclosedStatus.Public),
      street = address.street
        .filter(_ => this.statutDiffusionEtablissement == DisclosedStatus.Public),
      addressSupplement = address.addressSupplement
        .filter(_ => this.statutDiffusionEtablissement == DisclosedStatus.Public)
    )
  }

  def toSearchResult(activityLabel: Option[String], isMarketPlace: Boolean = false, filterAdress: Boolean = true) =
    EtablissementSearchResult(
      siret = siret,
      name = denominationUsuelleEtablissement,
      brand = enseigne1Etablissement.filter(!denominationUsuelleEtablissement.contains(_)),
      isHeadOffice = etablissementSiege.exists(_.toBoolean),
      address = if (filterAdress) toFilteredAddress() else toAddress(),
      activityCode = activitePrincipaleEtablissement,
      activityLabel = activityLabel,
      isMarketPlace = isMarketPlace,
      isOpen = etatAdministratifEtablissement.forall {
        case "O" => true
        case "F" => false
        case _   => true
      },
      isPublic = statutDiffusionEtablissement == DisclosedStatus.Public,
      lastUpdated = toOffsetDateTime(dateDernierTraitementEtablissement)
    )

}

object EtablissementData {
  implicit val format: OFormat[EtablissementData] = Json.format[EtablissementData]
  type EtablissementWithActivity = (EtablissementData, Option[ActivityCode])
}
