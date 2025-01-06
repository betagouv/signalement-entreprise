package config

import cats.implicits.catsSyntaxEitherId
import controllers.Token.HashedToken
import pureconfig.ConfigReader
import pureconfig.error.FailureReason

case class SignalConsoConfiguration(
    inseeToken: InseeTokenConfiguration,
    apiAuthenticationToken: HashedToken,
    publicDataOnly: Boolean
)

case class InseeTokenConfiguration(clientId: String, clientSecret: String, username: String, password: String)

object SignalConsoConfiguration {

  implicit val HashedTokenReader: ConfigReader[HashedToken] =
    ConfigReader
      .fromString(s => HashedToken(s).asRight: Either[FailureReason, HashedToken])

}
