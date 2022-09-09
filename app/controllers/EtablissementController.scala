package controllers

import models.SIRET
import orchestrators.EtablissementOrchestrator
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.AbstractController
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext

class EtablissementController(
    val etablissementOrchestrator: EtablissementOrchestrator,
    controllerComponents: ControllerComponents
)(implicit val ec: ExecutionContext)
    extends AbstractController(controllerComponents) {

  val logger: Logger = Logger(this.getClass)

  def searchEtablissement(q: String, postalCode: String) = Action.async { _ =>
    logger.debug(s"searchEtablissement $postalCode $q")
    etablissementOrchestrator
      .searchEtablissement(q, postalCode)
      .map(results => Ok(Json.toJson(results)))
  }

  def searchEtablissementByIdentity(identity: String) = Action.async { _ =>
    logger.debug(s"searchEtablissementByIdentity $identity")
    etablissementOrchestrator
      .searchEtablissementByIdentity(identity)
      .map(res => Ok(Json.toJson(res)))
  }

  def getBySiret() = Action.async(parse.json) { request =>
    for {
      sirets <- request.parseBody[List[SIRET]]()
      _ = logger.debug(s"get info by siret")
      res <- etablissementOrchestrator.getBySiret(sirets)
    } yield Ok(Json.toJson(res))
  }

}
