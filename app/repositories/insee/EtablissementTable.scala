package repositories.insee

import EtablissementTable.DENOMINATION_USUELLE_ETABLISSEMENT
import EtablissementTable.NOM_COMMERCIAL_ETABLISSEMENT
import models.EtablissementData
import models.SIREN
import models.SIRET
import models.insee.etablissement.DisclosedStatus
import repositories.PostgresProfile.api._
import slick.lifted.Rep
import slick.collection.heterogeneous.HNil
import slick.collection.heterogeneous.syntax._

import java.util.UUID

class EtablissementTable(tag: Tag) extends Table[EtablissementData](tag, "etablissements") {

  val id: Rep[UUID] = column[UUID]("id", O.PrimaryKey)
  def siret = column[SIRET]("siret", O.PrimaryKey) // Primary key MUST be there so insertOrUpdateAll will do his job
  def siren = column[SIREN]("siren")
  def dateDernierTraitementEtablissement = column[Option[String]]("datederniertraitementetablissement")
  def etablissementSiege = column[Option[String]]("etablissementsiege")
  def complementAdresseEtablissement = column[Option[String]]("complementadresseetablissement")
  def numeroVoieEtablissement = column[Option[String]]("numerovoieetablissement")
  def indiceRepetitionEtablissement = column[Option[String]]("indicerepetitionetablissement")
  def typeVoieEtablissement = column[Option[String]]("typevoieetablissement")
  def libelleVoieEtablissement = column[Option[String]]("libellevoieetablissement")
  def codePostalEtablissement = column[Option[String]]("codepostaletablissement")
  def libelleCommuneEtablissement = column[Option[String]]("libellecommuneetablissement")
  def libelleCommuneEtrangerEtablissement = column[Option[String]]("libellecommuneetrangeretablissement")
  def distributionSpecialeEtablissement = column[Option[String]]("distributionspecialeetablissement")
  def codeCommuneEtablissement = column[Option[String]]("codecommuneetablissement")
  def codeCedexEtablissement = column[Option[String]]("codecedexetablissement")
  def libelleCedexEtablissement = column[Option[String]]("libellecedexetablissement")
  def denominationUsuelleEtablissement = column[Option[String]](DENOMINATION_USUELLE_ETABLISSEMENT)
  def enseigne1Etablissement = column[Option[String]]("enseigne1etablissement")
  def activitePrincipaleEtablissement = column[Option[String]]("activiteprincipaleetablissement")
  def etatAdministratifEtablissement = column[Option[String]]("etatadministratifetablissement")
  def statutDiffusionEtablissement = column[DisclosedStatus]("statutdiffusionetablissement")
  def nomCommercialEtablissement = column[Option[String]](NOM_COMMERCIAL_ETABLISSEMENT)

  type EtablissementHList =
    UUID ::
      SIRET ::
      SIREN ::
      Option[String] ::
      Option[String] ::
      Option[String] ::
      Option[String] ::
      Option[String] ::
      Option[String] ::
      Option[String] ::
      Option[String] ::
      Option[String] ::
      Option[String] ::
      Option[String] ::
      Option[String] ::
      Option[String] ::
      Option[String] ::
      Option[String] ::
      Option[String] ::
      Option[String] ::
      Option[String] ::
      DisclosedStatus ::
      Option[String] ::
      HNil

  def constructEtablissement(etablissement: EtablissementHList): EtablissementData = etablissement match {
    case id ::
        siret ::
        siren ::
        dateDernierTraitementEtablissement ::
        etablissementSiege ::
        complementAdresseEtablissement ::
        numeroVoieEtablissement ::
        indiceRepetitionEtablissement ::
        typeVoieEtablissement ::
        libelleVoieEtablissement ::
        codePostalEtablissement ::
        libelleCommuneEtablissement ::
        libelleCommuneEtrangerEtablissement ::
        distributionSpecialeEtablissement ::
        codeCommuneEtablissement ::
        codeCedexEtablissement ::
        libelleCedexEtablissement ::
        denominationUsuelleEtablissement ::
        enseigne1Etablissement ::
        activitePrincipaleEtablissement ::
        etatAdministratifEtablissement ::
        statutDiffusionEtablissement ::
        nomCommercialEtablissement ::
        HNil =>
      EtablissementData(
        id = id,
        siret = siret,
        siren = siren,
        dateDernierTraitementEtablissement = dateDernierTraitementEtablissement,
        etablissementSiege = etablissementSiege,
        complementAdresseEtablissement = complementAdresseEtablissement,
        numeroVoieEtablissement = numeroVoieEtablissement,
        indiceRepetitionEtablissement = indiceRepetitionEtablissement,
        typeVoieEtablissement = typeVoieEtablissement,
        libelleVoieEtablissement = libelleVoieEtablissement,
        codePostalEtablissement = codePostalEtablissement,
        libelleCommuneEtablissement = libelleCommuneEtablissement,
        libelleCommuneEtrangerEtablissement = libelleCommuneEtrangerEtablissement,
        distributionSpecialeEtablissement = distributionSpecialeEtablissement,
        codeCommuneEtablissement = codeCommuneEtablissement,
        codeCedexEtablissement = codeCedexEtablissement,
        libelleCedexEtablissement = libelleCedexEtablissement,
        denominationUsuelleEtablissement = denominationUsuelleEtablissement,
        enseigne1Etablissement = enseigne1Etablissement,
        activitePrincipaleEtablissement = activitePrincipaleEtablissement,
        etatAdministratifEtablissement = etatAdministratifEtablissement,
        statutDiffusionEtablissement = statutDiffusionEtablissement,
        nomCommercialEtablissement = nomCommercialEtablissement
      )
  }

  def extractEtablissement(etablissement: EtablissementData): Option[EtablissementHList] =
    Some(
      etablissement.id ::
        etablissement.siret ::
        etablissement.siren ::
        etablissement.dateDernierTraitementEtablissement ::
        etablissement.etablissementSiege ::
        etablissement.complementAdresseEtablissement ::
        etablissement.numeroVoieEtablissement ::
        etablissement.indiceRepetitionEtablissement ::
        etablissement.typeVoieEtablissement ::
        etablissement.libelleVoieEtablissement ::
        etablissement.codePostalEtablissement ::
        etablissement.libelleCommuneEtablissement ::
        etablissement.libelleCommuneEtrangerEtablissement ::
        etablissement.distributionSpecialeEtablissement ::
        etablissement.codeCommuneEtablissement ::
        etablissement.codeCedexEtablissement ::
        etablissement.libelleCedexEtablissement ::
        etablissement.denominationUsuelleEtablissement ::
        etablissement.enseigne1Etablissement ::
        etablissement.activitePrincipaleEtablissement ::
        etablissement.etatAdministratifEtablissement ::
        etablissement.statutDiffusionEtablissement ::
        etablissement.nomCommercialEtablissement ::
        HNil
    )

  override def * = (
    id ::
      siret ::
      siren ::
      dateDernierTraitementEtablissement ::
      etablissementSiege ::
      complementAdresseEtablissement ::
      numeroVoieEtablissement ::
      indiceRepetitionEtablissement ::
      typeVoieEtablissement ::
      libelleVoieEtablissement ::
      codePostalEtablissement ::
      libelleCommuneEtablissement ::
      libelleCommuneEtrangerEtablissement ::
      distributionSpecialeEtablissement ::
      codeCommuneEtablissement ::
      codeCedexEtablissement ::
      libelleCedexEtablissement ::
      denominationUsuelleEtablissement ::
      enseigne1Etablissement ::
      activitePrincipaleEtablissement ::
      etatAdministratifEtablissement ::
      statutDiffusionEtablissement ::
      nomCommercialEtablissement ::
      HNil
  ) <> (constructEtablissement, extractEtablissement)
}

object EtablissementTable {
  val DENOMINATION_USUELLE_ETABLISSEMENT = "denominationusuelleetablissement"
  val NOM_COMMERCIAL_ETABLISSEMENT = "nomcommercialetablissement"
  val table = TableQuery[EtablissementTable]
}
