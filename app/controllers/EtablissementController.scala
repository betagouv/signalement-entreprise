package controllers

import cats.implicits.toShow
import controllers.EtablissementController.logWhenNoResult
import controllers.Logs.RichLogger
import controllers.Token.HashedToken
import controllers.Token.validateToken
import controllers.error.AppErrorTransformer.handleError
import models.SIRET
import models.api.EtablissementSearchResult
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
    val app = for {
      results <- etablissementOrchestrator.searchEtablissement(q, postalCode)
      _ = logWhenNoResult("search_company_postalcode_no_result")(results, Map("postalcode" -> postalCode, "name" -> q))
    } yield Ok(Json.toJson(results))

    app.recover { case err => handleError(request, err) }

  }

  def searchEtablissementByIdentity(identity: String, openOnly: Option[Boolean]) = Action.async { request =>
    val app = for {
      results <- etablissementOrchestrator
        .searchEtablissementByIdentity(identity, openOnly)
      _ = logWhenNoResult("search_company_identity_no_result")(
        results,
        Map("identity" -> identity, "openOnly" -> openOnly.show)
      )
    } yield Ok(Json.toJson(results))

    app.recover { case err => handleError(request, err) }
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

object EtablissementController {

  def logWhenNoResult(key: String)(list: List[EtablissementSearchResult], params: Map[String, String]): Unit =
    if (list.isEmpty) {
      logger
        .infoWithTitle(
          key,
          s"No result found with params : ${params})"
        )
    } else ()
}
