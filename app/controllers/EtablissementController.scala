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

import java.time.OffsetDateTime
import scala.concurrent.ExecutionContext
import scala.util.Try

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

  def searchEtablissementByIdentity(identity: String, withClosed: Option[Boolean]) = Action.async { request =>
    logger.debug(s"searchEtablissementByIdentity $identity")
    etablissementOrchestrator
      .searchEtablissementByIdentity(identity, withClosed)
      .map(res => Ok(Json.toJson(res)))
      .recover { case err => handleError(request, err) }
  }

  def getBySiret() = Action.async(parse.json) { request =>
    val res = for {
      _ <- validateToken(request, token)
      lastUpdated = request.queryString
        .get("lastUpdated")
        .flatMap(_.headOption)
        .flatMap(c => Try(OffsetDateTime.parse(c)).toOption)
      sirets <- request.parseBody[List[SIRET]]()
      _ = logger.debug(s"get info by siret")
      res <- etablissementOrchestrator.getBySiret(sirets, lastUpdated)
    } yield Ok(Json.toJson(res))
    res.recover { case err => handleError(request, err) }
  }

}
