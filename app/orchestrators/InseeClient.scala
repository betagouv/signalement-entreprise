package orchestrators

import config.InseeTokenConfiguration
import controllers.error.InseeEtablissementError
import controllers.error.InseeTokenGenerationError
import models.insee.etablissement.DisclosedStatus
import models.insee.etablissement.InseeEtablissementResponse
import models.insee.token.InseeEtablissementQuery
import models.insee.token.InseeTokenResponse
import orchestrators.InseeClient.EtablissementPageSize
import orchestrators.InseeClient.InitialCursor
import orchestrators.InseeClient.LastModifiedField
import orchestrators.InseeClient.WildCardPeriod
import play.api.Logger
import play.api.libs.json.JsError
import sttp.client3.playJson.asJson
import sttp.client3.HttpClientFutureBackend
import sttp.client3.Identity
import sttp.client3.RequestT
import sttp.client3.Response
import sttp.client3.ResponseException
import sttp.client3.UriContext
import sttp.client3.basicRequest
import sttp.model.StatusCode
import sttp.model.Uri
import cats.syntax.either._
import models.SIRET
import sttp.model.Uri.QuerySegment
import sttp.model.Uri.QuerySegment.KeyValue

import java.time.OffsetDateTime
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

/** Insee SIREN client see
  * https://api.insee.fr/catalogue/site/themes/wso2/subthemes/insee/pages/item-info.jag?name=Sirene&version=V3&provider=insee
  * *
  */
trait InseeClient {

  def generateToken(): Future[InseeTokenResponse]
  def getEtablissement(
      query: InseeEtablissementQuery,
      cursor: Option[String] = None
  ): Future[InseeEtablissementResponse]
}

class InseeClientImpl(inseeConfiguration: InseeTokenConfiguration)(implicit ec: ExecutionContext) extends InseeClient {

  val logger: Logger = Logger(this.getClass)

  import java.time.format.DateTimeFormatter

  val DateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")

  val backend = HttpClientFutureBackend()

  override def generateToken(): Future[InseeTokenResponse] = {
    val response: Future[Response[Either[ResponseException[String, JsError], InseeTokenResponse]]] =
      basicRequest
        .post(uri"https://api.insee.fr/token")
        .body("grant_type=client_credentials")
        .contentType("")
        .auth
        .basic(inseeConfiguration.key, inseeConfiguration.secret)
        .response(asJson[InseeTokenResponse])
        .send(backend)

    response
      .map(_.body)
      .flatMap {
        case Left(error) =>
          logger.error(error.getMessage, error)
          Future.failed(InseeTokenGenerationError(error.getMessage))
        case Right(token) =>
          Future.successful(token)
      }
  }

  override def getEtablissement(
      query: InseeEtablissementQuery,
      cursor: Option[String]
  ): Future[InseeEtablissementResponse] = {

    val req: RequestT[Identity, Either[String, String], Any] = basicRequest
      .get(buildUri(query.beginPeriod, cursor, query.endPeriod, query.siret, query.disclosedStatus))
      .auth
      .bearer(query.token.accessToken.value)

    logger.debug(req.toCurl(Set("Authorization")))

    val response: Future[Response[Either[ResponseException[String, JsError], InseeEtablissementResponse]]] =
      sendRequest(req)

    response
      .map(_.body)
      .flatMap(r => r.liftTo[Future])
  }

  private def buildUri(
      beginPeriod: Option[OffsetDateTime],
      cursor: Option[String],
      endPeriod: Option[OffsetDateTime],
      siret: Option[SIRET],
      disclosedStatus: Option[DisclosedStatus]
  ): Uri = {

    val beginPeriodAtStartOfDay: String = beginPeriod.map(_.format(DateFormatter)).getOrElse(WildCardPeriod)
    val endPeriodAtStartOfDay: String = endPeriod.map(_.format(DateFormatter)).getOrElse(WildCardPeriod)
    val cursorQueryParam: QuerySegment = KeyValue("curseur", cursor.getOrElse(InitialCursor))
    val sortQueryParam: QuerySegment = KeyValue("tri", LastModifiedField)
    val pageSizeQueryParam: QuerySegment = KeyValue("nombre", EtablissementPageSize.toString)
    val searchQueryParam = KeyValue(
      "q",
      s"""dateDernierTraitementEtablissement:[$beginPeriodAtStartOfDay TO $endPeriodAtStartOfDay]${disclosedStatus
          .map(s => s" AND statutDiffusionEtablissement:${s.entryName}")
          .getOrElse("")}${siret.map(s => s" AND siret:$s").getOrElse("")}"""
    )

    uri"https://api.insee.fr/entreprises/sirene/V3/siret"
      .addQuerySegment(searchQueryParam)
      .addQuerySegment(cursorQueryParam)
      .addQuerySegment(sortQueryParam)
      .addQuerySegment(pageSizeQueryParam)
  }

  def sendRequest(
      req: RequestT[Identity, Either[String, String], Any]
  ): Future[Response[Either[ResponseException[String, JsError], InseeEtablissementResponse]]] = {

    def response(): Future[Response[Either[ResponseException[String, JsError], InseeEtablissementResponse]]] = req
      .response(asJson[InseeEtablissementResponse])
      .send(backend)

    response().flatMap { r =>
      logger.info(s"Response : ${r.show(includeBody = false)}")
      logger.trace(s"Response : ${r.show()}")
      if (r.isSuccess) {
        Future.successful(r)
      } else {
        r.code match {
          case StatusCode.TooManyRequests =>
            logger.debug("Reaching API threshold (30 request/min) , waiting a bit to recover")
            Thread.sleep(60000)
            response()
          case StatusCode.NotFound =>
            Future.failed(
              InseeEtablissementError(
                s"No result found : ${r.body.swap.map(_.getMessage)}"
              )
            )
          case failedStatusCode =>
            logger.error(s" Failed status $failedStatusCode error ${r.show()}")
            Future.failed(
              InseeEtablissementError(
                s"Etablissement call failed with status code : ${r.show()}"
              )
            )
        }
      }
    }

  }
}

object InseeClient {

  val EtablissementPageSize = 1000
  val InitialCursor = "*"
  val LastModifiedField = "dateDernierTraitementEtablissement"
  val WildCardPeriod = "*"

}
