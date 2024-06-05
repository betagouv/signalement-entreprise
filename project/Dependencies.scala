import play.sbt.PlayImport.specs2

import sbt._

object Dependencies {
  object Versions {

    lazy val playSlickVersion          = "6.1.0"
    lazy val slickPgVersion            = "0.22.2"
    lazy val sentryVersion             = "6.34.0"
    lazy val specs2MatcherExtraVersion = "4.20.6"
    lazy val scalaCheckVersion         = "1.18.0"
    lazy val catsCoreVersion           = "2.12.0"
    lazy val pureConfigVersion         = "0.17.6"
    lazy val jacksonModuleScalaVersion = "2.17.1"
    lazy val enumeratumVersion         = "1.8.0"
    lazy val postgresqlVersion         = "42.7.3"
    lazy val refinedVersion            = "0.11.2"
    lazy val chimneyVersion            = "0.8.5"
    lazy val sttp                      = "3.9.7"
    lazy val flyWayVersion             = "10.14.0"
    lazy val janino                    = "3.1.12"
    // Cannot be updated to "7.4" because of the following error when logging as JSON:
    // java.lang.NoSuchMethodError: 'java.time.Instant ch.qos.logback.classic.spi.ILoggingEvent.getInstant()'
    // If we want to upgrade, we MUST check json logs (env var USE_TEXT_LOGS set to false) to see if this error still happen
    lazy val logstashLogbackEncoder = "7.3"

  }

  object Test {
    val specs2Import       = specs2            % "test"
    val specs2MatcherExtra = "org.specs2"     %% "specs2-matcher-extra" % Versions.specs2MatcherExtraVersion % "test"
    val scalaCheck         = "org.scalacheck" %% "scalacheck"           % Versions.scalaCheckVersion         % "test"

  }

  object Compile {
    val flywayCore     = "org.flywaydb" % "flyway-core"                % Versions.flyWayVersion
    val flywayPostgres = "org.flywaydb" % "flyway-database-postgresql" % Versions.flyWayVersion
    val janino = "org.codehaus.janino" % "janino" % Versions.janino // Needed for the <if> in logback conf
    val commonsCompiler = "org.codehaus.janino" % "commons-compiler" % Versions.janino // Needed for janino
    val logstashLogBackEncoder = "net.logstash.logback" % "logstash-logback-encoder" % Versions.logstashLogbackEncoder
    val sttpPlayJson    = "com.softwaremill.sttp.client3" %% "play-json"          % Versions.sttp
    val sttp            = "com.softwaremill.sttp.client3" %% "core"               % Versions.sttp
    val sentry          = "io.sentry"                      % "sentry-logback"     % Versions.sentryVersion
    val catsCore        = "org.typelevel"                 %% "cats-core"          % Versions.catsCoreVersion
    val pureConfig      = "com.github.pureconfig"         %% "pureconfig"         % Versions.pureConfigVersion
    val playSlick       = "org.playframework"             %% "play-slick"         % Versions.playSlickVersion
    val slickPg         = "com.github.tminglei"           %% "slick-pg"           % Versions.slickPgVersion
    val slickPgPlayJson = "com.github.tminglei"           %% "slick-pg_play-json" % Versions.slickPgVersion
    val jacksonModuleScala =
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % Versions.jacksonModuleScalaVersion
    val enumeratumPlay = "com.beachape"  %% "enumeratum-play" % Versions.enumeratumVersion
    val postgresql     = "org.postgresql" % "postgresql"      % Versions.postgresqlVersion
    val refinded       = "eu.timepit"    %% "refined"         % Versions.refinedVersion
    val chimney        = "io.scalaland"  %% "chimney"         % Versions.chimneyVersion
    val argon2Jvm      = "de.mkammerer"   % "argon2-jvm"      % "2.11"
  }

  val AppDependencies = Seq(
    Compile.janino,
    Compile.commonsCompiler,
    Compile.logstashLogBackEncoder,
    Compile.sttp,
    Compile.sttpPlayJson,
    Compile.sentry,
    Compile.catsCore,
    Compile.pureConfig,
    Compile.playSlick,
    Compile.enumeratumPlay,
    Compile.slickPg,
    Compile.slickPgPlayJson,
    Compile.jacksonModuleScala,
    Compile.postgresql,
    Compile.refinded,
    Compile.chimney,
    Compile.argon2Jvm,
    Compile.flywayCore,
    Compile.flywayPostgres,
    Test.specs2Import,
    Test.specs2MatcherExtra,
    Test.scalaCheck
  )
}
