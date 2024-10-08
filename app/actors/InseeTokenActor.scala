package actors

import actors.InseeTokenActor.FetchToken
import actors.InseeTokenActor.FetchTokenFailed
import actors.InseeTokenActor.FetchTokenSuccess
import actors.InseeTokenActor.GetToken
import actors.InseeTokenActor.GotToken
import actors.InseeTokenActor.RenewToken
import actors.InseeTokenActor.TokenError
import clients.InseeClient
import models.insee.token.InseeTokenResponse
import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl._

import scala.concurrent.duration.DurationInt
import scala.util.Failure
import scala.util.Success
import org.apache.pekko.actor.typed.scaladsl.adapter._
import org.apache.pekko.actor.Scheduler
import org.apache.pekko.pattern.retry
import play.api.Logger

object InseeTokenActor {
  sealed trait Command
  case class GetToken(replyTo: ActorRef[Reply])                                             extends Command
  case class RenewToken(replyTo: ActorRef[Reply])                                           extends Command
  private case class FetchToken(replyTo: ActorRef[Reply])                                   extends Command
  private case class FetchTokenSuccess(replyTo: ActorRef[Reply], token: InseeTokenResponse) extends Command
  private case class FetchTokenFailed(error: Throwable, replyTo: ActorRef[Reply])           extends Command

  sealed trait Reply
  case class GotToken(token: InseeTokenResponse) extends Reply
  case class TokenError(error: Throwable)        extends Reply

  def apply(inseeClient: InseeClient): Behavior[Command] =
    Behaviors.withStash(100) { buffer =>
      Behaviors.setup[Command] { context =>
        new InseeTokenActor(context, buffer, inseeClient).noToken()
      }
    }
}

class InseeTokenActor(
    context: ActorContext[InseeTokenActor.Command],
    buffer: StashBuffer[InseeTokenActor.Command],
    inseeClient: InseeClient
) {

  val logger: Logger = Logger(this.getClass)

  import context.executionContext
  implicit private val scheduler: Scheduler = context.system.scheduler.toClassic

  private def noToken(): Behaviors.Receive[InseeTokenActor.Command] =
    Behaviors.receiveMessagePartial { case GetToken(replyTo) =>
      logger.debug("Requesting a token while no token in cache, switch to fetching state")
      context.self ! FetchToken(replyTo)
      fetchToken()

    }

  private def fetchToken(): Behaviors.Receive[InseeTokenActor.Command] = Behaviors.receiveMessage {
    case FetchToken(replyTo) =>
      logger.debug("Fetching a fresh new token")
      val authenticateWithRetry = retry(() => inseeClient.generateToken(), 2, 500.milliseconds)
      context.pipeToSelf(authenticateWithRetry) {
        case Success(value) => FetchTokenSuccess(replyTo, value)
        case Failure(error) => FetchTokenFailed(error, replyTo)
      }
      fetchedToken()
    case other =>
      buffer.stash(other): Unit
      Behaviors.same
  }

  private def fetchedToken(): Behaviors.Receive[InseeTokenActor.Command] = Behaviors.receiveMessage {
    case FetchTokenSuccess(replyTo, value) =>
      logger.debug("Successfully fetched token")
      replyTo ! GotToken(value)
      buffer.unstashAll(hasToken(value))
    case FetchTokenFailed(error, replyTo) =>
      logger.debug(s"Fail to fetch token", error)
      replyTo ! TokenError(error)
      buffer.unstashAll(noToken())
    case other =>
      buffer.stash(other): Unit
      Behaviors.same
  }

  private def hasToken(token: InseeTokenResponse): Behaviors.Receive[InseeTokenActor.Command] =
    Behaviors.receiveMessagePartial {
      case GetToken(replyTo) =>
        logger.debug("Token requested")
        replyTo ! GotToken(token)
        Behaviors.same
      case RenewToken(replyTo) =>
        logger.debug("Renew token requested, switch to fetching state")
        context.self ! FetchToken(replyTo)
        fetchToken()
    }

}
