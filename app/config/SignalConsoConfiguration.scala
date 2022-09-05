package config

import java.net.URI

case class SignalConsoConfiguration(
    inseeToken: InseeTokenConfiguration
)

case class InseeTokenConfiguration(key: String, secret: String)
