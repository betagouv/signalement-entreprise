package repositories.insee

import config.SignalConsoConfiguration
import models.EtablissementData.Closed
import models.EtablissementData.Open
import models.ActivityCode
import models.EtablissementData
import models.SIREN
import models.SIRET
import models.insee.etablissement.DisclosedStatus
import models.insee.etablissement.DisclosedStatus.Public
import repositories.PostgresProfile.api._
import repositories.insee.EtablissementRepository.toOptionalSqlValue
import repositories.insee.EtablissementTable.DENOMINATION_USUELLE_ETABLISSEMENT
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.lifted.Rep
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class EtablissementRepository(val dbConfig: DatabaseConfig[JdbcProfile], conf: SignalConsoConfiguration)(implicit
    val ec: ExecutionContext
) extends EtablissementRepositoryInterface {

  val table: TableQuery[EtablissementTable] = EtablissementTable.table

  import dbConfig._

  private[this] def filterClosedEtablissements(row: EtablissementTable): Rep[Boolean] =
    row.etatAdministratifEtablissement.getOrElse(Open) =!= Closed

  override def insertOrUpdate(companies: Map[String, Option[String]]): Future[Int] = {

    val companyKeyValues: Map[String, String] =
      companies.view.mapValues(maybeValue => toOptionalSqlValue(maybeValue)).toMap
    val insertColumns: String = companyKeyValues.keys.mkString(",")
    val insertValues: String  = companyKeyValues.values.mkString(",")
    val insertValuesOnSiretConflict: String = companyKeyValues.view
      .filterKeys(_ != DENOMINATION_USUELLE_ETABLISSEMENT)
      .toMap
      .map { case (columnName, value) => s"$columnName = $value" }
      .mkString(",")
    db.run(sqlu"""INSERT INTO etablissements (#$insertColumns)
          VALUES (#$insertValues)
          ON CONFLICT(siret) DO UPDATE SET #$insertValuesOnSiretConflict
        """)
  }

  // Be careful when modifying this search, the order is important to use PG Indexes correctly.
  // Check the generated query with an EXPLAIN if necessary.
  override def search(
      q: String,
      postalCode: Option[String],
      onlyHeadOffice: Option[Boolean]
  ): Future[List[(EtablissementData, Option[ActivityCode])]] = {
    val setThreshold: DBIO[Int] =
      sqlu"""SET pg_trgm.word_similarity_threshold = 0.5""" // Higher is more restrictive, can be refined if necessary
    val searchQuery = table
      .filterOpt(onlyHeadOffice) { case (table, onlyHeadOffice) =>
        table.etablissementSiege === onlyHeadOffice.toString
      }
      .filterOpt(postalCode) { case (table, postalCode) => table.codePostalEtablissement === postalCode }
      .filter(filterClosedEtablissements)
      .filterIf(conf.filterNonDisclosed)(_.statutDiffusionEtablissement === (Public: DisclosedStatus))
      .filter(result =>
        result.searchColumnTrgm %> q
      )            // word similarity and not similarity because we use a composite search column
      .take(10000) // TO limit the following sort (can be really expensive). 10 000 is largely enough
      .joinLeft(ActivityCodeTable.table)
      .on(_.activitePrincipaleEtablissement === _.code)
      .sortBy { case (result, _) => result.searchColumnTrgm <->> q } // Sorting by word similarity dist
      .take(20)
      .to[List]
      .result

    db.run(
      (for {
        _       <- setThreshold
        results <- searchQuery
      } yield results).transactionally
    )
  }

  override def searchBySirets(
      sirets: List[SIRET]
  ): Future[List[(EtablissementData, Option[ActivityCode])]] =
    db.run(
      table
        .filter(_.siret inSetBind sirets)
        .joinLeft(ActivityCodeTable.table)
        .on(_.activitePrincipaleEtablissement === _.code)
        .to[List]
        .result
    )

  override def searchBySiretWithHeadOffice(
      siret: SIRET,
      openCompaniesOnly: Boolean
  ): Future[List[(EtablissementData, Option[ActivityCode])]] =
    db.run(
      table
        .filter(_.siren === SIREN(siret))
        .filter(company => company.siret === siret || company.etablissementSiege === "true")
        .filterIf(conf.filterNonDisclosed)(_.statutDiffusionEtablissement === (Public: DisclosedStatus))
        .filterIf(openCompaniesOnly)(filterClosedEtablissements)
        // We want to display the exact match first
        .sortBy(_.siret === siret)
        .joinLeft(ActivityCodeTable.table)
        .on(_.activitePrincipaleEtablissement === _.code)
        .to[List]
        .result
    ).map { result =>
      // If the siret is wrong but the siren exists, the query might return just a head office.
      // We should not return anything
      if (result.exists(_._1.siret == siret)) result else Nil
    }

  override def searchBySiren(
      siren: SIREN,
      openCompaniesOnly: Boolean
  ): Future[List[(EtablissementData, Option[ActivityCode])]] =
    db.run(
      table
        .filter(_.siren === siren)
        .filterIf(openCompaniesOnly)(filterClosedEtablissements)
        .filterIf(conf.filterNonDisclosed)(_.statutDiffusionEtablissement === (Public: DisclosedStatus))
        .joinLeft(ActivityCodeTable.table)
        .on(_.activitePrincipaleEtablissement === _.code)
        .to[List]
        .result
    )

  override def searchHeadOfficeBySiren(
      siren: SIREN,
      openCompaniesOnly: Boolean
  ): Future[Option[(EtablissementData, Option[ActivityCode])]] =
    db.run(
      table
        .filter(_.siren === siren)
        .filter(_.etablissementSiege === "true")
        .filterIf(openCompaniesOnly)(filterClosedEtablissements)
        .filterIf(conf.filterNonDisclosed)(_.statutDiffusionEtablissement === (Public: DisclosedStatus))
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
