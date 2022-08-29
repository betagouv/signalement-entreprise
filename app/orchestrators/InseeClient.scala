package orchestrators

import config.InseeTokenConfiguration
import controllers.error.InseeEtablissementError
import controllers.error.InseeTokenGenerationError
import models.insee.etablissement.InseeEtablissementResponse
import models.insee.token.InseeTokenResponse
import orchestrators.InseeClient.EtablissementPageSize
import orchestrators.InseeClient.InitialCursor
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
import cats.syntax.either._

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
      begin: OffsetDateTime,
      cursor: Option[String] = None
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
      beginPeriod: OffsetDateTime,
      cursor: Option[String]
  ): Future[InseeEtablissementResponse] = {

    beginPeriod.format(DateFormatter)
    val computedCursor: String = cursor.getOrElse(InitialCursor)

    val req: RequestT[Identity, Either[String, String], Any] = basicRequest
      .get(
//        uri"https://api.insee.fr/entreprises/sirene/V3/siret?q=dateDernierTraitementEtablissement:[$beginPeriodAtStartOfDay TO *]&nombre=$EtablissementPageSize&curseur=$computedCursor&tri=dateDernierTraitementEtablissement"
        uri"https://api.insee.fr/entreprises/sirene/V3/siret?q=statutDiffusionEtablissement:N&nombre=$EtablissementPageSize&curseur=$computedCursor&tri=dateDernierTraitementEtablissement"
      )
      .auth
      .bearer(token.accessToken.value)

    logger.debug(req.toCurl)

    val response: Future[Response[Either[ResponseException[String, JsError], InseeEtablissementResponse]]] =
      sendRequest(req)

    response
      .map(_.body)
      .flatMap(r => r.liftTo[Future])

  }

  def sendRequest(
      req: RequestT[Identity, Either[String, String], Any]
  ): Future[Response[Either[ResponseException[String, JsError], InseeEtablissementResponse]]] = {

    def response(): Future[Response[Either[ResponseException[String, JsError], InseeEtablissementResponse]]] = req
      .response(asJson[InseeEtablissementResponse])
      .send(backend)

    response().flatMap { r =>
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

}
