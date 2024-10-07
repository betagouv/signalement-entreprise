package clients

import cats.syntax.either._
import clients.InseeClient.EtablissementPageSize
import clients.InseeClient.InitialCursor
import clients.InseeClient.InseeClientError
import clients.InseeClient.LastModifiedField
import clients.InseeClient.TokenExpired
import clients.InseeClient.WildCardPeriod
import config.InseeTokenConfiguration
import controllers.error.InseeEtablissementError
import controllers.error.InseeTokenGenerationError
import models.SIRET
import models.insee.etablissement.DisclosedStatus
import models.insee.etablissement.InseeEtablissementResponse
import models.insee.token.InseeEtablissementQuery
import models.insee.token.InseeTokenResponse
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
import sttp.model.Uri.QuerySegment
import sttp.model.Uri.QuerySegment.KeyValue
import sttp.model.StatusCode
import sttp.model.Uri

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
      query: InseeEtablissementQuery,
      cursor: Option[String]
  ): Future[Either[InseeClientError, InseeEtablissementResponse]]
}

class InseeClientImpl(inseeConfiguration: InseeTokenConfiguration)(implicit ec: ExecutionContext) extends InseeClient {

  val logger: Logger = Logger(this.getClass)

  import java.time.format.DateTimeFormatter

  val DateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")

  val backend = HttpClientFutureBackend()

  override def generateToken(): Future[InseeTokenResponse] = {
    val response: Future[Response[Either[ResponseException[String, JsError], InseeTokenResponse]]] =
      basicRequest
        .post(uri"https://auth.insee.net/auth/realms/apim-gravitee/protocol/openid-connect/token")
        .body(
          Map(
            "grant_type"    -> "password",
            "client_id"     -> inseeConfiguration.clientId,
            "client_secret" -> inseeConfiguration.clientSecret,
            "username"      -> inseeConfiguration.username,
            "password"      -> inseeConfiguration.password
          )
        )
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
      query: InseeEtablissementQuery,
      cursor: Option[String]
  ): Future[Either[InseeClientError, InseeEtablissementResponse]] = {

    val req: RequestT[Identity, Either[String, String], Any] = basicRequest
      .get(buildUri(query.beginPeriod, cursor, query.endPeriod, query.siret, query.disclosedStatus))
      .auth
      .bearer(token.accessToken.value)

    logger.debug(req.toCurl(Set("Authorization")))

    sendRequest(req)
  }

  private def buildUri(
      beginPeriod: Option[OffsetDateTime],
      cursor: Option[String],
      endPeriod: Option[OffsetDateTime],
      siret: Option[SIRET],
      disclosedStatus: Option[DisclosedStatus]
  ): Uri = {

    val beginPeriodAtStartOfDay: String  = beginPeriod.map(_.format(DateFormatter)).getOrElse(WildCardPeriod)
    val endPeriodAtStartOfDay: String    = endPeriod.map(_.format(DateFormatter)).getOrElse(WildCardPeriod)
    val cursorQueryParam: QuerySegment   = KeyValue("curseur", cursor.getOrElse(InitialCursor))
    val sortQueryParam: QuerySegment     = KeyValue("tri", LastModifiedField)
    val pageSizeQueryParam: QuerySegment = KeyValue("nombre", EtablissementPageSize.toString)
    val searchQueryParam = KeyValue(
      "q",
      s"""dateDernierTraitementEtablissement:[$beginPeriodAtStartOfDay TO $endPeriodAtStartOfDay]${disclosedStatus
          .map(s => s" AND statutDiffusionEtablissement:${s.entryName}")
          .getOrElse("")}${siret.map(s => s" AND siret:$s").getOrElse("")}"""
    )

    uri"https://api.insee.fr/api-sirene/prive/3.11/siret"
      .addQuerySegment(searchQueryParam)
      .addQuerySegment(cursorQueryParam)
      .addQuerySegment(sortQueryParam)
      .addQuerySegment(pageSizeQueryParam)
  }

  def sendRequest(
      req: RequestT[Identity, Either[String, String], Any]
  ): Future[Either[InseeClientError, InseeEtablissementResponse]] =
    req
      .response(asJson[InseeEtablissementResponse])
      .send(backend)
      .flatMap { r =>
        logger.info(s"Response : ${r.show(includeBody = false)}")
        logger.trace(s"Response : ${r.show()}")
        if (r.isSuccess) {
          r.body.liftTo[Future].map(Right(_))
        } else {
          r.code match {
            case StatusCode.TooManyRequests =>
              logger.debug("Reaching API threshold (30 request/min) , waiting a bit to recover")
              Thread.sleep(60000)
              sendRequest(req)
            case StatusCode.NotFound =>
              Future.failed(
                InseeEtablissementError(
                  s"No result found : ${r.body.swap.map(_.getMessage)}"
                )
              )
            case StatusCode.Unauthorized | StatusCode.Forbidden =>
              Future.successful(Left(TokenExpired))
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

object InseeClient {

  val EtablissementPageSize = 1000
  val InitialCursor         = "*"
  val LastModifiedField     = "dateDernierTraitementEtablissement"
  val WildCardPeriod        = "*"

  sealed trait InseeClientError
  case object TokenExpired      extends InseeClientError
  case object RateLimitExceeded extends InseeClientError
}
