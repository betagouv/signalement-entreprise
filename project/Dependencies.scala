import play.sbt.PlayImport.specs2

import sbt._

object Dependencies {
  object Versions {

    lazy val playSlickVersion = "5.1.0"
    lazy val slickPgVersion = "0.21.1"
    lazy val sentryVersion = "6.10.0"
    lazy val specs2MatcherExtraVersion = "4.10.5"
    lazy val scalaCheckVersion = "1.17.0"
    lazy val catsCoreVersion = "2.9.0"
    lazy val pureConfigVersion = "0.17.2"
    lazy val jacksonModuleScalaVersion = "2.14.1"
    lazy val enumeratumVersion = "1.7.2"
    lazy val postgresqlVersion = "42.5.1"
    lazy val refinedVersion = "0.10.1"
    lazy val chimneyVersion = "0.6.2"
    lazy val sttp = "3.8.5"

  }

  object Test {
    val specs2Import = specs2 % "test"
    val specs2MatcherExtra = "org.specs2" %% "specs2-matcher-extra" % Versions.specs2MatcherExtraVersion % "test"
    val scalaCheck = "org.scalacheck" %% "scalacheck" % Versions.scalaCheckVersion % "test"

  }

  object Compile {
    val sttpPlayJson = "com.softwaremill.sttp.client3" %% "play-json" % "3.7.2"
    val sttp = "com.softwaremill.sttp.client3" %% "core" % Versions.sttp
    val sentry = "io.sentry" % "sentry-logback" % Versions.sentryVersion
    val catsCore = "org.typelevel" %% "cats-core" % Versions.catsCoreVersion
    val pureConfig = "com.github.pureconfig" %% "pureconfig" % Versions.pureConfigVersion
    val playSlick = "com.typesafe.play" %% "play-slick" % Versions.playSlickVersion
    val playSlickEvolutions = "com.typesafe.play" %% "play-slick-evolutions" % Versions.playSlickVersion
    val slickPg = "com.github.tminglei" %% "slick-pg" % Versions.slickPgVersion
    val slickPgPlayJson = "com.github.tminglei" %% "slick-pg_play-json" % Versions.slickPgVersion
    val jacksonModuleScala =
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % Versions.jacksonModuleScalaVersion
    val enumeratum = "com.beachape" %% "enumeratum" % Versions.enumeratumVersion
    val enumeratumPlay = "com.beachape" %% "enumeratum-play" % Versions.enumeratumVersion
    val postgresql = "org.postgresql" % "postgresql" % Versions.postgresqlVersion
    val refinded = "eu.timepit" %% "refined" % Versions.refinedVersion
    val chimney = "io.scalaland" %% "chimney" % Versions.chimneyVersion
    val argon2Jvm = "de.mkammerer" % "argon2-jvm" % "2.11"
  }

  val AppDependencies = Seq(
    Compile.sttp,
    Compile.sttpPlayJson,
    Compile.sentry,
    Compile.catsCore,
    Compile.pureConfig,
    Compile.playSlick,
    Compile.playSlickEvolutions,
    Compile.enumeratum,
    Compile.enumeratumPlay,
    Compile.slickPg,
    Compile.slickPgPlayJson,
    Compile.jacksonModuleScala,
    Compile.postgresql,
    Compile.refinded,
    Compile.chimney,
    Compile.argon2Jvm,
    Test.specs2Import,
    Test.specs2MatcherExtra,
    Test.scalaCheck
  )
}
