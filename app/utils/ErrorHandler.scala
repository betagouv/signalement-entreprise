package utils

import play.api._
import play.api.http.DefaultHttpErrorHandler
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import play.api.mvc.Results._
import play.api.mvc._
import play.api.routing.Router

import javax.inject.Provider
import scala.concurrent.Future

class ErrorHandler(
    env: Environment,
    config: Configuration,
    sourceMapper: OptionalSourceMapper,
    router: Provider[Router],
    val messagesApi: MessagesApi
) extends DefaultHttpErrorHandler(env, config, sourceMapper, router)
    with I18nSupport {

  override def onNotFound(request: RequestHeader, message: String): Future[Result] = Future.successful {
    NotFound
  }

  override def onProdServerError(request: RequestHeader, exception: UsefulException) = Future.successful {
    InternalServerError
  }
}
