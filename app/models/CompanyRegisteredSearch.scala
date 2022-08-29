package models

import utils.QueryStringMapper

import scala.util.Try

case class CompanyRegisteredSearch(
    departments: Seq[String] = Seq.empty[String],
    activityCodes: Seq[String] = Seq.empty[String],
    identity: Option[SearchCompanyIdentity] = None,
    emailsWithAccess: Option[String] = None
)

object CompanyRegisteredSearch {
  def fromQueryString(q: Map[String, Seq[String]]): Try[CompanyRegisteredSearch] = Try {
    val mapper = new QueryStringMapper(q)
    CompanyRegisteredSearch(
      departments = mapper.seq("departments"),
      activityCodes = mapper.seq("activityCodes"),
      emailsWithAccess = mapper.string("emailsWithAccess"),
      identity = mapper.string("identity").map(SearchCompanyIdentity.fromString)
    )
  }
}
