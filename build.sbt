name := "signalement-entreprise"
organization := "fr.gouv.beta"

version := "1.3.13"

scalaVersion := "2.13.8"

lazy val `signalement-entreprise` = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies ++= Seq(
  evolutions,
  ws,
  ehcache,
  compilerPlugin(scalafixSemanticdb)
) ++ Dependencies.AppDependencies

scalafmtOnCompile := true
scalacOptions ++= Seq(
  "-explaintypes",
  "-Ywarn-macros:after",
  "-Wconf:cat=unused-imports&src=views/.*:s",
  "-Wconf:cat=unused:info",
  s"-Wconf:src=${target.value}/.*:s",
  "-Yrangepos"
)

routesImport ++= Seq(
  "java.time.OffsetDateTime",
  "utils.SIRET",
  "controllers.UUIDPathBindable",
  "controllers.OffsetDateTimeQueryStringBindable",
  "controllers.SIRETPathBindable"
)

scalafixOnCompile := true

resolvers += "Atlassian Releases" at "https://packages.atlassian.com/maven-public/"

Universal / mappings ++=
  (baseDirectory.value / "appfiles" * "*" get) map
    (x => x -> ("appfiles/" + x.getName))

Test / javaOptions += "-Dconfig.resource=test.application.conf"
javaOptions += "-Dakka.http.parsing.max-uri-length=16k"
