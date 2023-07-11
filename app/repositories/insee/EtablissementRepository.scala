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

  private val least = SimpleFunction.ternary[Option[Double], Option[Double], Option[Double], Option[Double]]("least")

  private[this] def filterClosedEtablissements(row: EtablissementTable): Rep[Boolean] =
    row.etatAdministratifEtablissement.getOrElse(Open) =!= Closed

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
        .filterIf(conf.filterNonDisclosed)(_.statutDiffusionEtablissement === (Public: DisclosedStatus))
        .filter(result =>
          least(
            result.denominationUsuelleEtablissement <-> q,
            result.nomCommercialEtablissement <-> q,
            result.enseigne1Etablissement <-> q
          ).map(dist => dist < 0.68).getOrElse(false)
        )
        .sortBy(result =>
          least(
            result.denominationUsuelleEtablissement <-> q,
            result.nomCommercialEtablissement <-> q,
            result.enseigne1Etablissement <-> q
          )
        )
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

  override def searchBySiretWithHeadOffice(
      siret: SIRET,
      openCompaniesOnly: Boolean
  ): Future[List[(EtablissementData, Option[ActivityCode])]] =
    db.run(
      table
        .filter(_.siren === SIREN(siret))
        .filter(company => company.siret === siret || company.etablissementSiege === "true")
        .filter(_.denominationUsuelleEtablissement.isDefined)
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
        .filter(_.denominationUsuelleEtablissement.isDefined)
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
        .filter(_.denominationUsuelleEtablissement.isDefined)
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
