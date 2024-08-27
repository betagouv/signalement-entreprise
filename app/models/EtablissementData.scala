package models

import models.EtablissementData.Open
import models.api.Address
import models.api.EtablissementSearchResult
import models.insee.ForeignCountry
import models.insee.etablissement.DisclosedStatus
import orchestrators.toOffsetDateTime

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
    codePaysEtrangerEtablissement: Option[String],
    distributionSpecialeEtablissement: Option[String],
    codeCommuneEtablissement: Option[String],
    codeCedexEtablissement: Option[String],
    libelleCedexEtablissement: Option[String],
    denomination: Option[String],
    denominationUsuelle1UniteLegale: Option[String],
    denominationUsuelle2UniteLegale: Option[String],
    denominationUsuelle3UniteLegale: Option[String],
    nomCommercialEtablissement: Option[String],
    enseigne1Etablissement: Option[String],
    enseigne2Etablissement: Option[String],
    enseigne3Etablissement: Option[String],
    activitePrincipaleEtablissement: Option[String],
    etatAdministratifEtablissement: Option[String],
    statutDiffusionEtablissement: DisclosedStatus
) {

  def isOpen = this.etatAdministratifEtablissement.getOrElse(Open) == Open

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
    addressSupplement = complementAdresseEtablissement,
    country = codePaysEtrangerEtablissement
      .flatMap(_.toIntOption)
      .flatMap(inseeCode => ForeignCountry.map.get(inseeCode).map(_.code))
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

  private[models] def computeEnseigne: Option[String] = {
    val list = List(enseigne1Etablissement, enseigne2Etablissement, enseigne3Etablissement).flatten.filterNot(_.isBlank)
    if (list.isEmpty) None
    else Some(list.mkString(" - "))
  }

  private[models] def computeCommercialName: Option[String] = {
    val list =
      List(denominationUsuelle1UniteLegale, denominationUsuelle2UniteLegale, denominationUsuelle3UniteLegale).flatten
        .filterNot(_.isBlank)
    if (list.isEmpty) None
    else Some(list.mkString(" - "))
  }

  def toSearchResult(activityLabel: Option[String], isMarketPlace: Boolean = false, filterAdress: Boolean = true) =
    EtablissementSearchResult(
      siret = siret,
      name = denomination,
      commercialName = computeCommercialName,
      establishmentCommercialName = nomCommercialEtablissement,
      brand = computeEnseigne,
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
  type EtablissementWithActivity = (EtablissementData, Option[ActivityCode])
  val Closed = "F"
  val Open   = "A"
}
