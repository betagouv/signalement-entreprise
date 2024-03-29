import play.sbt.PlayImport.specs2

import sbt._

object Dependencies {
  object Versions {

    lazy val playSlickVersion          = "5.1.0"
    lazy val slickPgVersion            = "0.21.1"
    lazy val sentryVersion             = "6.34.0"
    lazy val specs2MatcherExtraVersion = "4.20.3"
    lazy val scalaCheckVersion         = "1.17.0"
    lazy val catsCoreVersion           = "2.10.0"
    lazy val pureConfigVersion         = "0.17.4"
    lazy val jacksonModuleScalaVersion = "2.16.0"
    lazy val enumeratumVersion         = "1.7.3"
    lazy val postgresqlVersion         = "42.5.1"
    lazy val refinedVersion            = "0.11.0"
    lazy val chimneyVersion            = "0.8.3"
    lazy val sttp                      = "3.9.1"
    lazy val flyWayVersion             = "10.0.1"
    lazy val janino                    = "3.1.11"
    lazy val logstashLogbackEncoder    = "7.3"

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
    val playSlick       = "com.typesafe.play"             %% "play-slick"         % Versions.playSlickVersion
    val slickPg         = "com.github.tminglei"           %% "slick-pg"           % Versions.slickPgVersion
    val slickPgPlayJson = "com.github.tminglei"           %% "slick-pg_play-json" % Versions.slickPgVersion
    val jacksonModuleScala =
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % Versions.jacksonModuleScalaVersion
    val enumeratum     = "com.beachape"  %% "enumeratum"      % Versions.enumeratumVersion
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
    Compile.enumeratum,
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
