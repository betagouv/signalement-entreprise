package company.entrepriseimportinfo

import company.EnterpriseImportInfo
import repositories.PostgresProfile
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import java.time.OffsetDateTime
import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class EnterpriseImportInfoRepository(val dbConfig: DatabaseConfig[JdbcProfile])(implicit
    ec: ExecutionContext
) {

  import PostgresProfile.api._
  import dbConfig._

  val EnterpriseSyncInfotableQuery = TableQuery[EnterpriseImportInfoTable]

  def create(info: EnterpriseImportInfo): Future[EnterpriseImportInfo] =
    db.run(EnterpriseSyncInfotableQuery += info).map(_ => info)

  def byId(id: UUID) = EnterpriseSyncInfotableQuery.filter(_.id === id)

  def updateLinesDone(id: UUID, linesDone: Double, lastUpdated: Option[OffsetDateTime]): Future[Int] = {
    val query = lastUpdated match {
      case Some(lastUpdate) =>
        byId(id)
          .map(t => (t.linesDone, t.lastUpdated))
          .update((linesDone, Some(lastUpdate)))
      case None =>
        byId(id)
          .map(t => t.linesDone)
          .update(linesDone)
    }
    db.run(query)
  }

  def updateEndedAt(id: UUID, endAt: OffsetDateTime = OffsetDateTime.now()): Future[Int] =
    db.run(byId(id).map(_.endedAt).update(Some(endAt)))

  def updateError(id: UUID, error: String): Future[Int] =
    db.run(byId(id).map(_.errors).update(Some(error)))

  def updateAllEndedAt(name: String, endAt: OffsetDateTime = OffsetDateTime.now()): Future[Int] =
    db.run(
      EnterpriseSyncInfotableQuery
        .filter(_.endedAt.isEmpty)
        .filter(_.fileName === name)
        .map(_.endedAt)
        .update(Some(endAt))
    )

  def updateAllError(name: String, error: String): Future[Int] =
    db.run(
      EnterpriseSyncInfotableQuery
        .filter(_.endedAt.isEmpty)
        .filter(_.fileName === name)
        .map(_.errors)
        .update(Some(error))
    )

  def findRunning(name: String): Future[Option[EnterpriseImportInfo]] =
    db.run(
      EnterpriseSyncInfotableQuery
        .filter(_.fileName === name)
        .filter(_.endedAt.isEmpty)
        .sortBy(_.startedAt.desc)
        .result
        .headOption
    )

  def findLastEnded(name: String): Future[Option[EnterpriseImportInfo]] =
    db.run(
      EnterpriseSyncInfotableQuery
        .filter(_.fileName === name)
        .filter(_.endedAt.isDefined)
        .sortBy(_.startedAt.desc)
        .result
        .headOption
    )

  def findLast(name: String): Future[Option[EnterpriseImportInfo]] =
    db.run(
      EnterpriseSyncInfotableQuery
        .filter(_.fileName === name)
        .sortBy(_.startedAt.desc)
        .result
        .headOption
    )
}
