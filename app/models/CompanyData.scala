package models

import play.api.libs.json.Json
import play.api.libs.json.OFormat
import utils.SIREN
import utils.SIRET

import java.util.UUID

case class CompanyData(
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
    etatAdministratifEtablissement: Option[String]
) {
  def toAddress: Address = Address(
    number = numeroVoieEtablissement,
    street = Option(
      Seq(
        typeVoieEtablissement.flatMap(typeVoie => TypeVoies.values.find(_._1 == typeVoie).map(_._2.toUpperCase)),
        libelleVoieEtablissement
      ).flatten
    ).filterNot(_.isEmpty).map(_.mkString(" ")),
    postalCode = codePostalEtablissement,
    city = libelleCommuneEtablissement,
    addressSupplement = complementAdresseEtablissement
  )

  def toSearchResult(activityLabel: Option[String], isMarketPlace: Boolean = false) = CompanySearchResult(
    siret = siret,
    name = denominationUsuelleEtablissement,
    brand = enseigne1Etablissement.filter(!denominationUsuelleEtablissement.contains(_)),
    isHeadOffice = etablissementSiege.exists(_.toBoolean),
    address = toAddress,
    activityCode = activitePrincipaleEtablissement,
    activityLabel = activityLabel,
    isMarketPlace = isMarketPlace,
    isOpen = etatAdministratifEtablissement.forall {
      case "O" => true
      case "F" => false
      case _   => true
    }
  )
}

object CompanyData {
  implicit val format: OFormat[CompanyData] = Json.format[CompanyData]
}
