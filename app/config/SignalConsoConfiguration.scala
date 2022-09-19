package config

import cats.implicits.catsSyntaxEitherId
import controllers.Token.HashedToken
import pureconfig.ConfigReader
import pureconfig.error.FailureReason

case class SignalConsoConfiguration(
    inseeToken: InseeTokenConfiguration,
    apiAuthenticationToken: HashedToken
)

case class InseeTokenConfiguration(key: String, secret: String)

object SignalConsoConfiguration {

  implicit val HashedTokenReader: ConfigReader[HashedToken] =
    ConfigReader
      .fromString(s => HashedToken(s).asRight: Either[FailureReason, HashedToken])

}
