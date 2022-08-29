package orchestrators

import company.companydata.CompanyDataRepositoryInterface
import models.CompanySearchResult
import play.api.Logger
import utils.SIREN
import utils.SIRET

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class CompanyOrchestrator(
    val companyDataRepository: CompanyDataRepositoryInterface
)(implicit ec: ExecutionContext) {

  val logger: Logger = Logger(this.getClass)

  def searchCompany(q: String, postalCode: String): Future[List[CompanySearchResult]] = {
    logger.debug(s"searchCompany $postalCode $q")
    companyDataRepository
      .search(q, postalCode)
      .map(results => results.map(result => result._1.toSearchResult(result._2.map(_.label))))
  }

  def searchCompanyByIdentity(identity: String): Future[List[CompanySearchResult]] = {
    logger.debug(s"searchCompanyByIdentity $identity")

    (identity.replaceAll("\\s", "") match {
      case q if q.matches(SIRET.pattern) =>
        companyDataRepository.searchBySiretIncludingHeadOfficeWithActivity(SIRET.fromUnsafe(q))
      case q =>
        SIREN.pattern.r
          .findFirstIn(q)
          .map(siren =>
            for {
              headOffice <- companyDataRepository.searchHeadOfficeBySiren(SIREN(siren))
              companies <- headOffice
                .map(company => Future(List(company)))
                .getOrElse(companyDataRepository.searchBySiren(SIREN(siren)))
            } yield companies
          )
          .getOrElse(Future(List.empty))
    }).map(companiesWithActivity =>
      companiesWithActivity.map { case (company, activity) =>
        company.toSearchResult(activity.map(_.label))
      }
    )
  }

  def getBySiret(sirets: List[SIRET]): Future[List[CompanySearchResult]] =
    companyDataRepository
      .searchBySirets(sirets, includeClosed = true)
      .map(companies =>
        companies.map { case (companyData, maybeActivity) =>
          companyData.toSearchResult(maybeActivity.map(_.label))
        }
      )

}
