package company.companydata

import models.CompanyActivity
import repositories.PostgresProfile.api._

class CompanyActivityTable(tag: Tag) extends Table[CompanyActivity](tag, "activites") {
  def code = column[String]("code")

  def libelle = column[String]("libelle")

  def * = (code, libelle) <> ((CompanyActivity.apply _).tupled, CompanyActivity.unapply)
}

object CompanyActivityTable {
  val table = TableQuery[CompanyActivityTable]
}
