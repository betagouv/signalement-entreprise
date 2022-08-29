package orchestrators

import config.InseeTokenConfiguration
import controllers.error.InseeEtablissementError
import controllers.error.InseeTokenGenerationError
import models.insee.etablissement.DisclosedStatus
import models.insee.etablissement.InseeEtablissementResponse
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
      token: InseeTokenResponse,
      beginPeriod: Option[OffsetDateTime] = None,
      cursor: Option[String] = None,
      endPeriod: Option[OffsetDateTime] = None,
      disclosedStatus: Option[DisclosedStatus] = None
  ): Future[InseeEtablissementResponse]
}

class InseeClientImpl(inseeConfiguration: InseeTokenConfiguration)(implicit ec: ExecutionContext) extends InseeClient {

  val logger: Logger = Logger(this.getClass)

  import java.time.format.DateTimeFormatter

  val DateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

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
      token: InseeTokenResponse,
      beginPeriod: Option[OffsetDateTime],
      cursor: Option[String],
      endPeriod: Option[OffsetDateTime] = None,
      disclosedStatus: Option[DisclosedStatus] = None
  ): Future[InseeEtablissementResponse] = {

    val req: RequestT[Identity, Either[String, String], Any] = basicRequest
      .get(buildUri(beginPeriod, cursor, endPeriod, disclosedStatus))
      .auth
      .bearer(token.accessToken.value)

    logger.debug(req.toCurl(Set.empty[String]))

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
          .getOrElse("")}"""
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
    // recover effect
    // .recoverWith()

  }
}

object InseeClient {

  val EtablissementPageSize = 1000
  val InitialCursor = "*"
  val LastModifiedField = "dateDernierTraitementEtablissement"
  val WildCardPeriod = "*"

}
