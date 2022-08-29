package config

import java.net.URI

case class SignalConsoConfiguration(
    tmpDirectory: String,
    apiURL: URI,
    inseeToken: InseeTokenConfiguration
)

case class InseeTokenConfiguration(key: String, secret: String)
