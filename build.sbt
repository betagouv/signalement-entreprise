import org.typelevel.scalacoptions.ScalacOptions

name         := "signalement-entreprise"
organization := "fr.gouv.beta"

version := "1.3.14"

scalaVersion := "2.13.15"

lazy val `signalement-entreprise` = (project in file(".")).enablePlugins(PlayScala).enablePlugins(BuildInfoPlugin)

// Commit hash: COMMIT_ID is provided at build time, fallback to local git
lazy val gitCommit: String =
  sys.env
    .get("COMMIT_ID")
    .orElse(scala.util.Try(scala.sys.process.Process("git rev-parse HEAD").!!.trim).toOption)
    .filter(_.nonEmpty)
    .getOrElse("unknown")

buildInfoKeys := Seq[BuildInfoKey](
  name,
  version,
  BuildInfoKey.action("gitCommit")(gitCommit),
  BuildInfoKey.action("buildTime")(java.time.Instant.now().toString)
)
buildInfoPackage := "buildinfo"

libraryDependencies ++= Seq(
  ws,
  ehcache,
  compilerPlugin(scalafixSemanticdb)
) ++ Dependencies.AppDependencies

scalafmtOnCompile := true
scalacOptions ++= Seq(
  "-explaintypes",
  "-Ywarn-macros:after",
  "-release:17",
  "-Wconf:cat=unused-imports&src=views/.*:s",
  "-Wconf:cat=unused:info",
  s"-Wconf:src=${target.value}/.*:s",
  "-Yrangepos"
)

scalafixOnCompile := true

resolvers += "Atlassian Releases" at "https://packages.atlassian.com/maven-public/"

Universal / mappings ++=
  (baseDirectory.value / "appfiles" * "*" get) map
    (x => x -> ("appfiles/" + x.getName))

Test / javaOptions += "-Dconfig.resource=test.application.conf"
javaOptions += "-Dpekko.http.parsing.max-uri-length=16k"
javaOptions += s"-Dtextlogs=${sys.env.getOrElse("USE_TEXT_LOGS", "false")}"

routesImport ++= Seq("controllers.LocaleQueryStringBindable")

Test / tpolecatExcludeOptions += ScalacOptions.warnNonUnitStatement
