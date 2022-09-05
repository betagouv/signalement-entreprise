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

  def updateLineCount(id: UUID, lineCount: Double): Future[Int] =
    db.run(byId(id).map(_.linesCount).update(lineCount))

  def updateEndedAt(id: UUID, endAt: OffsetDateTime = OffsetDateTime.now()): Future[Int] =
    db.run(byId(id).map(_.endedAt).update(Some(endAt)))

  def updateError(id: UUID, endAt: OffsetDateTime = OffsetDateTime.now(), error: String): Future[Int] =
    db.run(
      byId(id)
        .map { j =>
          (j.errors, j.endedAt)
        }
        .update((Some(error), Some(endAt)))
    )

  def findRunning() =
    db.run(
      EnterpriseSyncInfotableQuery
        .filter(_.endedAt.isEmpty)
        .sortBy(_.startedAt.desc)
        .result
    )

  def findLastEnded(): Future[Option[EnterpriseImportInfo]] =
    db.run(
      EnterpriseSyncInfotableQuery
        .filter(_.lastUpdated.isDefined)
        .sortBy(_.lastUpdated.desc)
        .result
        .headOption
    )
}
