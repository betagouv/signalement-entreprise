package repositories.insee

import models.ActivityCode
import repositories.PostgresProfile.api._

class ActivityCodeTable(tag: Tag) extends Table[ActivityCode](tag, "activites") {
  def code = column[String]("code")

  def libelle = column[String]("libelle")

  def * = (code, libelle) <> ((ActivityCode.apply _).tupled, ActivityCode.unapply)
}

object ActivityCodeTable {
  val table = TableQuery[ActivityCodeTable]
}
