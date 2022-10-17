package models.insee.etablissement

import models.Siren
import models.Siret
import play.api.libs.json.Json
import play.api.libs.json.OFormat

case class InseeEtablissement(
    siret: Siret,
    siren: Siren,
    nic: String,
    statutDiffusionEtablissement: String,
    dateCreationEtablissement: Option[String],
    trancheEffectifsEtablissement: Option[String],
    anneeEffectifsEtablissement: Option[String],
    activitePrincipaleRegistreMetiersEtablissement: Option[String],
    dateDernierTraitementEtablissement: Option[String],
    etablissementSiege: Boolean,
    nombrePeriodesEtablissement: Int,
    uniteLegale: UniteLegale,
    adresseEtablissement: AdresseEtablissement,
    adresse2Etablissement: Option[Adresse2Etablissement],
    periodesEtablissement: List[PeriodeEtablissement]
) {

  def lastPeriodeEtablissement: Option[PeriodeEtablissement] =
    this.periodesEtablissement.filter(_.dateFin.isEmpty) match {
      case Nil         => None
      case ::(head, _) => Some(head)
    }

  def toMap(denomination: String): Map[String, Option[String]] = {

    val lastPeriodeEtablissement: Option[PeriodeEtablissement] = this.lastPeriodeEtablissement

    Map(
      "siren" -> Some(this.siren.toString),
      "nic" -> Some(this.nic),
      "siret" -> Some(this.siret.toString),
      "statutdiffusionetablissement" -> Some(this.statutDiffusionEtablissement),
      "datecreationetablissement" -> this.dateCreationEtablissement,
      "trancheeffectifsetablissement" -> this.trancheEffectifsEtablissement,
      "anneeeffectifsetablissement" -> this.anneeEffectifsEtablissement,
      "activiteprincipaleregistremetiersetablissement" -> this.activitePrincipaleRegistreMetiersEtablissement,
      "datederniertraitementetablissement" -> this.dateDernierTraitementEtablissement,
      "etablissementsiege" -> Some(this.etablissementSiege.toString),
      "nombreperiodesetablissement" -> Some(nombrePeriodesEtablissement.toString),
      "complementadresseetablissement" -> this.adresseEtablissement.complementAdresseEtablissement,
      "numerovoieetablissement" -> this.adresseEtablissement.numeroVoieEtablissement,
      "indicerepetitionetablissement" -> this.adresseEtablissement.indiceRepetitionEtablissement,
      "typevoieetablissement" -> this.adresseEtablissement.typeVoieEtablissement,
      "libellevoieetablissement" -> this.adresseEtablissement.libelleVoieEtablissement,
      "codepostaletablissement" -> this.adresseEtablissement.codePostalEtablissement,
      "libellecommuneetablissement" -> this.adresseEtablissement.libelleCommuneEtablissement,
      "libellecommuneetrangeretablissement" -> this.adresseEtablissement.libelleCommuneEtrangerEtablissement,
      "distributionspecialeetablissement" -> this.adresseEtablissement.distributionSpecialeEtablissement,
      "codecommuneetablissement" -> this.adresseEtablissement.codeCommuneEtablissement,
      "codecedexetablissement" -> this.adresseEtablissement.codeCedexEtablissement,
      "libellecedexetablissement" -> this.adresseEtablissement.libelleCedexEtablissement,
      "codepaysetrangeretablissement" -> this.adresseEtablissement.codePaysEtrangerEtablissement,
      "libellepaysetrangeretablissement" -> this.adresseEtablissement.libellePaysEtrangerEtablissement,
      "complementadresse2etablissement" -> this.adresse2Etablissement.flatMap(_.complementAdresse2Etablissement),
      "numerovoie2etablissement" -> this.adresse2Etablissement.flatMap(_.numeroVoie2Etablissement),
      "indicerepetition2etablissement" -> this.adresse2Etablissement.flatMap(_.indiceRepetition2Etablissement),
      "typevoie2etablissement" -> this.adresse2Etablissement.flatMap(_.typeVoie2Etablissement),
      "libellevoie2etablissement" -> this.adresse2Etablissement.flatMap(_.libelleVoie2Etablissement),
      "codepostal2etablissement" -> this.adresse2Etablissement.flatMap(_.codePostal2Etablissement),
      "libellecommune2etablissement" -> this.adresse2Etablissement.flatMap(_.libelleCommune2Etablissement),
      "libellecommuneetranger2etablissement" -> this.adresse2Etablissement
        .flatMap(_.libelleCommuneEtranger2Etablissement),
      "distributionspeciale2etablissement" -> this.adresse2Etablissement.flatMap(_.distributionSpeciale2Etablissement),
      "codecommune2etablissement" -> this.adresse2Etablissement.flatMap(_.codeCommune2Etablissement),
      "codecedex2etablissement" -> this.adresse2Etablissement.flatMap(_.codeCedex2Etablissement),
      "libellecedex2etablissement" -> this.adresse2Etablissement.flatMap(_.libelleCedex2Etablissement),
      "codepaysetranger2etablissement" -> this.adresse2Etablissement.flatMap(_.codePaysEtranger2Etablissement),
      "libellepaysetranger2etablissement" -> this.adresse2Etablissement.flatMap(_.libellePaysEtranger2Etablissement),
      "datedebut" -> lastPeriodeEtablissement.flatMap(_.dateDebut),
      "enseigne1etablissement" -> lastPeriodeEtablissement.flatMap(_.enseigne1Etablissement),
      "enseigne2etablissement" -> lastPeriodeEtablissement.flatMap(_.enseigne2Etablissement),
      "enseigne3etablissement" -> lastPeriodeEtablissement.flatMap(_.enseigne3Etablissement),
      "denominationusuelleetablissement" -> Some(denomination),
      "activiteprincipaleetablissement" -> lastPeriodeEtablissement.flatMap(_.activitePrincipaleEtablissement),
      "nomenclatureactiviteprincipaleetablissement" -> lastPeriodeEtablissement.flatMap(
        _.nomenclatureActivitePrincipaleEtablissement
      ),
      "caractereemployeuretablissement" -> lastPeriodeEtablissement.flatMap(_.caractereEmployeurEtablissement),
      "etatadministratifetablissement" -> lastPeriodeEtablissement.flatMap(_.etatAdministratifEtablissement)
    )

  }

}

object InseeEtablissement {

  implicit val format: OFormat[InseeEtablissement] = Json.format[InseeEtablissement]

  def withNonEmpty(obj: InseeEtablissement) =
    InseeEtablissement(
      siret = obj.siret,
      siren = obj.siren,
      nic = obj.nic,
      statutDiffusionEtablissement = obj.statutDiffusionEtablissement,
      dateCreationEtablissement = obj.dateCreationEtablissement.withTrimmedNonEmpty,
      trancheEffectifsEtablissement = obj.trancheEffectifsEtablissement.withTrimmedNonEmpty,
      anneeEffectifsEtablissement = obj.anneeEffectifsEtablissement.withTrimmedNonEmpty,
      activitePrincipaleRegistreMetiersEtablissement =
        obj.activitePrincipaleRegistreMetiersEtablissement.withTrimmedNonEmpty,
      dateDernierTraitementEtablissement = obj.dateDernierTraitementEtablissement.withTrimmedNonEmpty,
      etablissementSiege = obj.etablissementSiege,
      nombrePeriodesEtablissement = obj.nombrePeriodesEtablissement,
      uniteLegale = UniteLegale.withNonEmpty(obj.uniteLegale),
      adresseEtablissement = AdresseEtablissement.withNonEmpty(obj.adresseEtablissement),
      adresse2Etablissement = obj.adresse2Etablissement.map(Adresse2Etablissement.withNonEmpty),
      periodesEtablissement = obj.periodesEtablissement.map(PeriodeEtablissement.withNonEmpty)
    )

}
