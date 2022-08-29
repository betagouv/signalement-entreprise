import cats.syntax.either._

import controllers.error.ApiError.MalformedBody
import controllers.error.ApiError.MalformedId
import play.api.Logger
import play.api.libs.json.JsError
import play.api.libs.json.JsPath
import play.api.libs.json.JsValue
import play.api.libs.json.Reads
import play.api.mvc.PathBindable
import play.api.mvc.QueryStringBindable
import play.api.mvc.Request
import utils.DateUtils
import utils.SIRET

import java.time.OffsetDateTime
import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Try

package object controllers {

  val logger: Logger = Logger(this.getClass)

  implicit val OffsetDateTimeQueryStringBindable: QueryStringBindable[OffsetDateTime] =
    QueryStringBindable.bindableString
      .transform[OffsetDateTime](
        stringOffsetDateTime => DateUtils.parseTime(stringOffsetDateTime),
        offsetDateTime => offsetDateTime.toString
      )

  implicit val UUIDPathBindable =
    PathBindable.bindableString
      .transform[UUID](
        id => extractUUID(id),
        uuid => uuid.toString
      )

  implicit val SIRETPathBindable =
    PathBindable.bindableString
      .transform[SIRET](
        siret => SIRET(siret),
        siret => siret.value
      )

  def extractUUID(stringUUID: String): UUID =
    Try(UUID.fromString(stringUUID)).fold(
      { e =>
        logger.error(s"Unable to parse $stringUUID to UUID", e)
        throw MalformedId(stringUUID)
      },
      identity
    )

  implicit class RequestOps[T <: JsValue](request: Request[T])(implicit ec: ExecutionContext) {
    def parseBody[B](path: JsPath = JsPath())(implicit reads: Reads[B]) = request.body
      .validate[B](path.read[B])
      .asEither
      .leftMap { errors =>
        logger.error(
          s"Malformed request body path ${path} [error : ${JsError.toJson(errors)} , body ${request.body} ]"
        )
        MalformedBody
      }
      .liftTo[Future]
  }
}
