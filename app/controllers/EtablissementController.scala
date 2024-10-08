package controllers

import cats.implicits.toShow
import controllers.EtablissementController.logWhenNoResult
import controllers.Logs.RichLogger
import controllers.Token.HashedToken
import controllers.Token.validateToken
import controllers.error.AppErrorTransformer.handleError
import models.ImportRequest
import models.SIREN
import models.SIRET
import models.api.EtablissementSearchResult
import orchestrators.EtablissementImportService
import orchestrators.EtablissementService
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.AbstractController
import play.api.mvc.ControllerComponents

import java.time.OffsetDateTime
import java.util.Locale
import scala.concurrent.ExecutionContext
import scala.util.Try

class EtablissementController(
    val etablissementOrchestrator: EtablissementService,
    importService: EtablissementImportService,
    controllerComponents: ControllerComponents,
    token: HashedToken
)(implicit val ec: ExecutionContext)
    extends AbstractController(controllerComponents) {

  val logger: Logger = Logger(this.getClass)

  def searchSmart(
      q: String,
      postalCode: Option[String],
      departmentCode: Option[String],
      lang: Locale
  ) = Action.async { request =>
    val app = for {
      results <- etablissementOrchestrator.searchSmart(q, postalCode, departmentCode, lang)
      _ = logWhenNoResult("search_smart_no_result")(
        results,
        List(
          postalCode.map(a => "postalcode" -> a),
          departmentCode.map(a => "departmentCode" -> a),
          Some("lang" -> lang.toLanguageTag),
          Some("name" -> q)
        ).flatten.toMap
      )
    } yield Ok(Json.toJson(results))
    app.recover { case err => handleError(request, err) }
  }

  def searchEtablissement(
      q: String,
      postalCode: Option[String],
      onlyHeadOffice: Option[Boolean],
      lang: Option[Locale]
  ) = Action.async { request =>
    val app = for {
      results <- etablissementOrchestrator.searchEtablissement(q, postalCode, onlyHeadOffice, lang)
      _ = logWhenNoResult("search_company_postalcode_no_result")(
        results,
        List(
          postalCode.map(a => "postalcode" -> a),
          onlyHeadOffice.map(a => "onlyHeadOffice" -> a.toString),
          lang.map(a => "lang" -> a.toLanguageTag),
          Some("name" -> q)
        ).flatten.toMap
      )
    } yield Ok(Json.toJson(results))

    app.recover { case err => handleError(request, err) }

  }

  def searchEtablissementByIdentity(identity: String, openOnly: Option[Boolean], lang: Option[Locale]) = Action.async {
    request =>
      val app = for {
        results <- etablissementOrchestrator
          .searchEtablissementByIdentity(identity, openOnly, lang)
        _ = logWhenNoResult("search_company_identity_no_result")(
          results,
          Map("identity" -> identity, "openOnly" -> openOnly.show)
        )
      } yield Ok(Json.toJson(results))

      app.recover { case err => handleError(request, err) }
  }

  def getBySiret(lang: Option[Locale]) = Action.async(parse.json) { request =>
    val res = for {
      _ <- validateToken(request, token)
      lastUpdated = request.queryString
        .get("lastUpdated")
        .flatMap(_.headOption)
        .flatMap(c => Try(OffsetDateTime.parse(c)).toOption)
      sirets <- request.parseBody[List[SIRET]]()
      _ = logger.debug(s"get info by siret")
      res <- etablissementOrchestrator.getBySiret(sirets, lastUpdated, lang)
    } yield Ok(Json.toJson(res))
    res.recover { case err => handleError(request, err) }
  }

  def getBySiren(lang: Option[Locale], onlyHeadOffice: Option[Boolean]) = Action.async(parse.json) { request =>
    val res = for {
      _      <- validateToken(request, token)
      sirens <- request.parseBody[List[SIREN]]()
      _ = logger.debug(s"get info by siren")
      res <- etablissementOrchestrator.getBySiren(sirens, lang, onlyHeadOffice)
    } yield Ok(Json.toJson(res))
    res.recover { case err => handleError(request, err) }
  }

  def importEtablissements() = Action.async(parse.json) { request =>
    val res = for {
      _             <- validateToken(request, token)
      importRequest <- request.parseBody[ImportRequest]()
      _ = logger.debug(s"Import etablissements $importRequest")
      _ <- importService.runImportEtablissementsRequest(importRequest)
    } yield NoContent
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
