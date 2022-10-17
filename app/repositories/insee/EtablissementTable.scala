package repositories.insee

import EtablissementTable.DENOMINATION_USUELLE_ETABLISSEMENT
import models.EtablissementData
import models.Siren
import models.Siret

import models.insee.etablissement.DisclosedStatus
import repositories.PostgresProfile.api._
import slick.lifted.Rep

import java.util.UUID

class EtablissementTable(tag: Tag) extends Table[EtablissementData](tag, "etablissements") {

  val id: Rep[UUID] = column[UUID]("id", O.PrimaryKey)
  def siret = column[Siret]("siret", O.PrimaryKey) // Primary key MUST be there so insertOrUpdateAll will do his job
  def siren = column[Siren]("siren")
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

  def * = (
    id,
    siret,
    siren,
    dateDernierTraitementEtablissement,
    etablissementSiege,
    complementAdresseEtablissement,
    numeroVoieEtablissement,
    indiceRepetitionEtablissement,
    typeVoieEtablissement,
    libelleVoieEtablissement,
    codePostalEtablissement,
    libelleCommuneEtablissement,
    libelleCommuneEtrangerEtablissement,
    distributionSpecialeEtablissement,
    codeCommuneEtablissement,
    codeCedexEtablissement,
    libelleCedexEtablissement,
    denominationUsuelleEtablissement,
    enseigne1Etablissement,
    activitePrincipaleEtablissement,
    etatAdministratifEtablissement,
    statutDiffusionEtablissement
  ) <> ((EtablissementData.apply _).tupled, EtablissementData.unapply)
}

object EtablissementTable {
  val DENOMINATION_USUELLE_ETABLISSEMENT = "denominationusuelleetablissement"
  val table = TableQuery[EtablissementTable]
}
