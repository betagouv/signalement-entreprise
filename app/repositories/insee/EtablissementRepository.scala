package repositories.insee

import EtablissementRepository.toOptionalSqlValue
import EtablissementTable.DENOMINATION_USUELLE_ETABLISSEMENT
import models.SIREN
import models.SIRET
import models.EtablissementData
import models.insee.etablissement.DisclosedStatus
import repositories.PostgresProfile.api._
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery

import DisclosedStatus.Public

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class EtablissementRepository(val dbConfig: DatabaseConfig[JdbcProfile])(implicit
    val ec: ExecutionContext
) extends EtablissementRepositoryInterface {

  val table: TableQuery[EtablissementTable] = EtablissementTable.table

  import dbConfig._

  private val least = SimpleFunction.binary[Option[Double], Option[Double], Option[Double]]("least")

  private[this] def filterClosedEtablissements(row: EtablissementTable): Rep[Boolean] =
    row.etatAdministratifEtablissement.getOrElse("A") =!= "F"

  override def insertOrUpdate(companies: Map[String, Option[String]]): Future[Int] = {

    val companyKeyValues: Map[String, String] =
      companies.view.mapValues(maybeValue => toOptionalSqlValue(maybeValue)).toMap
    val insertColumns: String = companyKeyValues.keys.mkString(",")
    val insertValues: String = companyKeyValues.values.mkString(",")
    val insertValuesOnSiretConflict: String = companyKeyValues.view
      .filterKeys(_ != DENOMINATION_USUELLE_ETABLISSEMENT)
      .toMap
      .map { case (columnName, value) => s"$columnName = $value" }
      .mkString(",")
    db.run(sqlu"""INSERT INTO etablissements (#$insertColumns)
          VALUES (#$insertValues)
          ON CONFLICT(siret) DO UPDATE SET #$insertValuesOnSiretConflict,
          denominationusuelleetablissement=COALESCE(NULLIF(#${companyKeyValues.getOrElse(
        DENOMINATION_USUELLE_ETABLISSEMENT,
        "NULL"
      )}, ''), etablissements.denominationusuelleetablissement)
        """)
  }

  override def search(q: String, postalCode: String): Future[List[(EtablissementData, Option[ActivityCode])]] =
    db.run(
      table
        .filter(_.codePostalEtablissement === postalCode)
        .filter(_.denominationUsuelleEtablissement.isDefined)
        .filter(filterClosedEtablissements)
        .filter(_.statutDiffusionEtablissement === (Public: DisclosedStatus))
        .filter(result =>
          least(
            result.denominationUsuelleEtablissement <-> q,
            result.enseigne1Etablissement <-> q
          ).map(dist => dist < 0.68).getOrElse(false)
        )
        .sortBy(result => least(result.denominationUsuelleEtablissement <-> q, result.enseigne1Etablissement <-> q))
        .take(10)
        .joinLeft(ActivityCodeTable.table)
        .on(_.activitePrincipaleEtablissement === _.code)
        .to[List]
        .result
    )

  override def searchBySirets(
      sirets: List[SIRET]
  ): Future[List[(EtablissementData, Option[ActivityCode])]] =
    db.run(
      table
        .filter(_.siret inSetBind sirets)
        .filter(_.denominationUsuelleEtablissement.isDefined)
        .joinLeft(ActivityCodeTable.table)
        .on(_.activitePrincipaleEtablissement === _.code)
        .to[List]
        .result
    )

  override def searchBySiretIncludingHeadOfficeWithActivity(
      siret: SIRET
  ): Future[List[(EtablissementData, Option[ActivityCode])]] =
    db.run(
      table
        .filter(_.siren === SIREN(siret))
        .filter(company => company.siret === siret || company.etablissementSiege === "true")
        .filter(_.denominationUsuelleEtablissement.isDefined)
        .filter(_.statutDiffusionEtablissement === (Public: DisclosedStatus))
        .filter(filterClosedEtablissements)
        .joinLeft(ActivityCodeTable.table)
        .on(_.activitePrincipaleEtablissement === _.code)
        .to[List]
        .result
    )

  override def searchBySiren(
      siren: SIREN
  ): Future[List[(EtablissementData, Option[ActivityCode])]] =
    db.run(
      table
        .filter(_.siren === siren)
        .filter(_.denominationUsuelleEtablissement.isDefined)
        .filter(filterClosedEtablissements)
        .filter(_.statutDiffusionEtablissement === (Public: DisclosedStatus))
        .joinLeft(ActivityCodeTable.table)
        .on(_.activitePrincipaleEtablissement === _.code)
        .to[List]
        .result
    )

  override def searchHeadOfficeBySiren(
      siren: SIREN
  ): Future[Option[(EtablissementData, Option[ActivityCode])]] =
    db.run(
      table
        .filter(_.siren === siren)
        .filter(_.etablissementSiege === "true")
        .filter(_.denominationUsuelleEtablissement.isDefined)
        .filter(filterClosedEtablissements)
        .filter(_.statutDiffusionEtablissement === (Public: DisclosedStatus))
        .joinLeft(ActivityCodeTable.table)
        .on(_.activitePrincipaleEtablissement === _.code)
        .to[List]
        .result
    ).map(_.headOption)
}

object EtablissementRepository {

  def toOptionalSqlValue(maybeValue: Option[String]): String = maybeValue.fold("NULL")(value => toSqlValue(value))

  def toSqlValue(value: String): String = s"'${value.replace("'", "''")}'"

}
