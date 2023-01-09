package config

case class ApplicationConfiguration(
    app: SignalConsoConfiguration,
    flyway: FlywayConfiguration
)
