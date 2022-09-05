package config


case class SignalConsoConfiguration(
    inseeToken: InseeTokenConfiguration
)

case class InseeTokenConfiguration(key: String, secret: String)
