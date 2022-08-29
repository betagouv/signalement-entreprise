package controllers

import orchestrators.CompanyOrchestrator
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.AbstractController
import play.api.mvc.ControllerComponents
import utils.SIRET

import scala.concurrent.ExecutionContext

class CompanyController(
    val companyOrchestrator: CompanyOrchestrator,
    controllerComponents: ControllerComponents
)(implicit val ec: ExecutionContext)
    extends AbstractController(controllerComponents) {

  val logger: Logger = Logger(this.getClass)

  def searchCompany(q: String, postalCode: String) = Action.async { _ =>
    logger.debug(s"searchCompany $postalCode $q")
    companyOrchestrator
      .searchCompany(q, postalCode)
      .map(results => Ok(Json.toJson(results)))
  }

  def searchCompanyByIdentity(identity: String) = Action.async { _ =>
    logger.debug(s"searchCompanyByIdentity $identity")
    companyOrchestrator
      .searchCompanyByIdentity(identity)
      .map(res => Ok(Json.toJson(res)))
  }

  def getBySiret() = Action.async(parse.json) { request =>
    for {
      sirets <- request.parseBody[List[SIRET]]()
      _ = logger.debug(s"get info by siret")
      res <- companyOrchestrator.getBySiret(sirets)
    } yield Ok(Json.toJson(res))
  }

}
