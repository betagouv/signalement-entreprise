package repositories.insee

import EtablissementTable.DENOMINATION_USUELLE_ETABLISSEMENT
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

  def codePaysEtrangerEtablissement = column[Option[String]]("codepaysetrangeretablissement")

  def distributionSpecialeEtablissement = column[Option[String]]("distributionspecialeetablissement")

  def codeCommuneEtablissement = column[Option[String]]("codecommuneetablissement")

  def codeCedexEtablissement = column[Option[String]]("codecedexetablissement")

  def libelleCedexEtablissement = column[Option[String]]("libellecedexetablissement")

  def denomination = column[Option[String]](DENOMINATION_USUELLE_ETABLISSEMENT)

  def denominationUsuelle1UniteLegale = column[Option[String]]("denominationusuelle1unitelegale")

  def denominationUsuelle2UniteLegale = column[Option[String]]("denominationusuelle2unitelegale")

  def denominationUsuelle3UniteLegale = column[Option[String]]("denominationusuelle3unitelegale")

  def nomCommercialEtablissement = column[Option[String]]("nomcommercialetablissement")

  def enseigne1Etablissement = column[Option[String]]("enseigne1etablissement")

  def enseigne2Etablissement = column[Option[String]]("enseigne2etablissement")

  def enseigne3Etablissement = column[Option[String]]("enseigne3etablissement")

  def activitePrincipaleEtablissement = column[Option[String]]("activiteprincipaleetablissement")

  def etatAdministratifEtablissement = column[Option[String]]("etatadministratifetablissement")

  def statutDiffusionEtablissement = column[DisclosedStatus]("statutdiffusionetablissement")

  def searchColumnTrgm = column[String]("search_column_trgm")

  def codeDepartement = column[Option[String]]("codedepartement")

  def libellePaysEtrangerEtablissement = column[Option[String]]("libellepaysetrangeretablissement")

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
      Option[String] ::
      Option[String] ::
      Option[String] ::
      Option[String] ::
      Option[String] ::
      Option[String] ::
      Option[String] ::
      DisclosedStatus ::
      Option[String] ::
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
        codePaysEtrangerEtablissement ::
        distributionSpecialeEtablissement ::
        codeCommuneEtablissement ::
        codeCedexEtablissement ::
        libelleCedexEtablissement ::
        denomination ::
        denominationUsuelle1UniteLegale ::
        denominationUsuelle2UniteLegale ::
        denominationUsuelle3UniteLegale ::
        nomCommercialEtablissement ::
        enseigne1Etablissement ::
        enseigne2Etablissement ::
        enseigne3Etablissement ::
        activitePrincipaleEtablissement ::
        etatAdministratifEtablissement ::
        statutDiffusionEtablissement ::
        codeDepartement ::
        libellePaysEtrangerEtablissement ::
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
        codePaysEtrangerEtablissement = codePaysEtrangerEtablissement,
        distributionSpecialeEtablissement = distributionSpecialeEtablissement,
        codeCommuneEtablissement = codeCommuneEtablissement,
        codeCedexEtablissement = codeCedexEtablissement,
        libelleCedexEtablissement = libelleCedexEtablissement,
        denomination = denomination,
        denominationUsuelle1UniteLegale = denominationUsuelle1UniteLegale,
        denominationUsuelle2UniteLegale = denominationUsuelle2UniteLegale,
        denominationUsuelle3UniteLegale = denominationUsuelle3UniteLegale,
        nomCommercialEtablissement = nomCommercialEtablissement,
        enseigne1Etablissement = enseigne1Etablissement,
        enseigne2Etablissement = enseigne2Etablissement,
        enseigne3Etablissement = enseigne3Etablissement,
        activitePrincipaleEtablissement = activitePrincipaleEtablissement,
        etatAdministratifEtablissement = etatAdministratifEtablissement,
        statutDiffusionEtablissement = statutDiffusionEtablissement,
        codeDepartement = codeDepartement,
        libellePaysEtrangerEtablissement = libellePaysEtrangerEtablissement
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
        etablissement.codePaysEtrangerEtablissement ::
        etablissement.distributionSpecialeEtablissement ::
        etablissement.codeCommuneEtablissement ::
        etablissement.codeCedexEtablissement ::
        etablissement.libelleCedexEtablissement ::
        etablissement.denomination ::
        etablissement.denominationUsuelle1UniteLegale ::
        etablissement.denominationUsuelle2UniteLegale ::
        etablissement.denominationUsuelle3UniteLegale ::
        etablissement.nomCommercialEtablissement ::
        etablissement.enseigne1Etablissement ::
        etablissement.enseigne2Etablissement ::
        etablissement.enseigne3Etablissement ::
        etablissement.activitePrincipaleEtablissement ::
        etablissement.etatAdministratifEtablissement ::
        etablissement.statutDiffusionEtablissement ::
        etablissement.codeDepartement ::
        etablissement.libellePaysEtrangerEtablissement ::
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
      codePaysEtrangerEtablissement ::
      distributionSpecialeEtablissement ::
      codeCommuneEtablissement ::
      codeCedexEtablissement ::
      libelleCedexEtablissement ::
      denomination ::
      denominationUsuelle1UniteLegale ::
      denominationUsuelle2UniteLegale ::
      denominationUsuelle3UniteLegale ::
      nomCommercialEtablissement ::
      enseigne1Etablissement ::
      enseigne2Etablissement ::
      enseigne3Etablissement ::
      activitePrincipaleEtablissement ::
      etatAdministratifEtablissement ::
      statutDiffusionEtablissement ::
      codeDepartement ::
      libellePaysEtrangerEtablissement ::
      HNil
  ) <> (constructEtablissement, extractEtablissement)
}

object EtablissementTable {
  val DENOMINATION_USUELLE_ETABLISSEMENT = "denomination"
  val table                              = TableQuery[EtablissementTable]
}
