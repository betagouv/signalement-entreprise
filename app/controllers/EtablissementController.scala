package controllers

import controllers.Token.HashedToken
import controllers.Token.validateToken
import controllers.error.AppErrorTransformer.handleError
import models.SIRET
import orchestrators.EtablissementService
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.AbstractController
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext

class EtablissementController(
    val etablissementOrchestrator: EtablissementService,
    controllerComponents: ControllerComponents,
    token: HashedToken
)(implicit val ec: ExecutionContext)
    extends AbstractController(controllerComponents) {

  val logger: Logger = Logger(this.getClass)

  def searchEtablissement(q: String, postalCode: String) = Action.async { request =>
    logger.debug(s"searchEtablissement $postalCode $q")
    etablissementOrchestrator
      .searchEtablissement(q, postalCode)
      .map(results => Ok(Json.toJson(results)))
      .recover { case err => handleError(request, err) }
  }

  def searchEtablissementByIdentity(identity: String) = Action.async { request =>
    logger.debug(s"searchEtablissementByIdentity $identity")
    etablissementOrchestrator
      .searchEtablissementByIdentity(identity)
      .map(res => Ok(Json.toJson(res)))
      .recover { case err => handleError(request, err) }
  }

  def getBySiret() = Action.async(parse.json) { request =>
    val res = for {
      _ <- validateToken(request, token)
      sirets <- request.parseBody[List[SIRET]]()
      _ = logger.debug(s"get info by siret")
      res <- etablissementOrchestrator.getBySiret(sirets)
    } yield Ok(Json.toJson(res))
    res.recover { case err => handleError(request, err) }
  }

}
