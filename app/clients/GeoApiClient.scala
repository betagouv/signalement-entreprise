package clients
import cats.syntax.either._
import controllers.error.GeoApiError
import models.GeoApiCommune
import play.api.Logger

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import sttp.client3.playJson.asJson
import sttp.client3.HttpClientFutureBackend
import sttp.client3.UriContext
import sttp.client3.basicRequest

// Doc https://guides.data.gouv.fr/reutiliser-des-donnees/utiliser-les-api-geographiques/utiliser-lapi-decoupage-administratif
// Url https://geo.api.gouv.fr/communes
class GeoApiClient(implicit ec: ExecutionContext) {

  val logger: Logger = Logger(this.getClass)
  val backend        = HttpClientFutureBackend()

  def getAllCommunes(): Future[Seq[GeoApiCommune]] = {
    val req = basicRequest
      .get(uri"https://geo.api.gouv.fr/communes")
    logger.debug(req.toCurl)
    req
      .response(asJson[List[GeoApiCommune]])
      .send(backend)
      .flatMap { r =>
        logger.info(s"Response : ${r.show(includeBody = false)}")
        logger.trace(s"Response : ${r.show()}")
        if (r.isSuccess) {
          Future.successful(r)
        } else {
          logger.error(s" Failed status ${r.code} error ${r.show()}")
          Future.failed(
            GeoApiError(
              s"Geo api call failed with status code : ${r.show()}"
            )
          )
        }
      }
      .map(_.body)
      .flatMap(r => r.liftTo[Future])
  }
}
