package company.companydata

import company.companydata.CompanyDataRepository.toOptionalSqlValue
import company.companydata.CompanyDataTable.DENOMINATION_USUELLE_ETABLISSEMENT
import models.CompanyActivity
import models.CompanyData
import repositories.CRUDRepository
import repositories.PostgresProfile.api._
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery
import utils.SIREN
import utils.SIRET

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class CompanyDataRepository(override val dbConfig: DatabaseConfig[JdbcProfile])(implicit
    override val ec: ExecutionContext
) extends CRUDRepository[CompanyDataTable, CompanyData]
    with CompanyDataRepositoryInterface {

  override val table: TableQuery[CompanyDataTable] = CompanyDataTable.table

  import dbConfig._

  private val least = SimpleFunction.binary[Option[Double], Option[Double], Option[Double]]("least")

  private[this] def filterClosedEtablissements(row: CompanyDataTable): Rep[Boolean] =
    row.etatAdministratifEtablissement.getOrElse("A") =!= "F"

  def insertOrUpdate(companies: Map[String, Option[String]]): Future[Int] =
    db.run(
      insertAll(companies)
    )

  private def insertAll(companies: Map[String, Option[String]]): DBIO[Int] = {

    val companyKeyValues: Map[String, String] =
      companies.view.mapValues(maybeValue => toOptionalSqlValue(maybeValue)).toMap
    val insertColumns: String = companyKeyValues.keys.mkString(",")
    val insertValues: String = companyKeyValues.values.mkString(",")
    val insertValuesOnSiretConflict: String = companyKeyValues.view
      .filterKeys(_ != DENOMINATION_USUELLE_ETABLISSEMENT)
      .toMap
      .map { case (columnName, value) => s"$columnName = $value" }
      .mkString(",")

    sqlu"""INSERT INTO etablissements (#$insertColumns)
          VALUES (#$insertValues)
          ON CONFLICT(siret) DO UPDATE SET #$insertValuesOnSiretConflict,
          denominationusuelleetablissement=COALESCE(NULLIF(#${companyKeyValues.getOrElse(
        DENOMINATION_USUELLE_ETABLISSEMENT,
        "NULL"
      )}, ''), etablissements.denominationusuelleetablissement)
        """
  }

  override def search(q: String, postalCode: String): Future[List[(CompanyData, Option[CompanyActivity])]] =
    db.run(
      table
        .filter(_.codePostalEtablissement === postalCode)
        .filter(_.denominationUsuelleEtablissement.isDefined)
        .filter(filterClosedEtablissements)
        .filter(result =>
          least(
            result.denominationUsuelleEtablissement <-> q,
            result.enseigne1Etablissement <-> q
          ).map(dist => dist < 0.68).getOrElse(false)
        )
        .sortBy(result => least(result.denominationUsuelleEtablissement <-> q, result.enseigne1Etablissement <-> q))
        .take(10)
        .joinLeft(CompanyActivityTable.table)
        .on(_.activitePrincipaleEtablissement === _.code)
        .to[List]
        .result
    )

  override def searchBySirets(
      sirets: List[SIRET],
      includeClosed: Boolean = false
  ): Future[List[(CompanyData, Option[CompanyActivity])]] =
    db.run(
      table
        .filter(_.siret inSetBind sirets)
        .filter(_.denominationUsuelleEtablissement.isDefined)
        .filterIf(!includeClosed)(filterClosedEtablissements)
        .joinLeft(CompanyActivityTable.table)
        .on(_.activitePrincipaleEtablissement === _.code)
        .to[List]
        .result
    )

  override def searchBySiretIncludingHeadOfficeWithActivity(
      siret: SIRET
  ): Future[List[(CompanyData, Option[CompanyActivity])]] =
    db.run(
      table
        .filter(_.siren === SIREN(siret))
        .filter(company => company.siret === siret || company.etablissementSiege === "true")
        .filter(_.denominationUsuelleEtablissement.isDefined)
        .filter(filterClosedEtablissements)
        .joinLeft(CompanyActivityTable.table)
        .on(_.activitePrincipaleEtablissement === _.code)
        .to[List]
        .result
    )

  private def searchBySirens(
      sirens: List[SIREN],
      includeClosed: Boolean = false
  ): Future[List[(CompanyData, Option[CompanyActivity])]] =
    db.run(
      table
        .filter(_.siren inSetBind sirens)
        .filter(_.denominationUsuelleEtablissement.isDefined)
        .filterIf(!includeClosed)(filterClosedEtablissements)
        .joinLeft(CompanyActivityTable.table)
        .on(_.activitePrincipaleEtablissement === _.code)
        .to[List]
        .result
    )

  override def searchBySiren(
      siren: SIREN
  ): Future[List[(CompanyData, Option[CompanyActivity])]] =
    searchBySirens(List(siren))

  override def searchHeadOfficeBySiren(siren: SIREN): Future[Option[(CompanyData, Option[CompanyActivity])]] =
    searchHeadOfficeBySiren(List(siren)).map(_.headOption)

  private def searchHeadOfficeBySiren(
      sirens: List[SIREN],
      includeClosed: Boolean = false
  ): Future[List[(CompanyData, Option[CompanyActivity])]] =
    db.run(
      table
        .filter(_.siren inSetBind sirens)
        .filter(_.etablissementSiege === "true")
        .filter(_.denominationUsuelleEtablissement.isDefined)
        .filterIf(!includeClosed)(filterClosedEtablissements)
        .joinLeft(CompanyActivityTable.table)
        .on(_.activitePrincipaleEtablissement === _.code)
        .to[List]
        .result
    )
}

object CompanyDataRepository {

  def toOptionalSqlValue(maybeValue: Option[String]): String = maybeValue.fold("NULL")(value => toSqlValue(value))

  def toSqlValue(value: String): String = s"'${value.replace("'", "''")}'"

}
