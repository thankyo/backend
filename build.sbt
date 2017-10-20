import play.sbt.routes.RoutesKeys
import sbt.Keys.{libraryDependencies, resolvers}

import scala.io.Source

name := "backend"

version := "1.0-SNAPSHOT"

scalaVersion := "2.12.3"
scalacOptions ++= Seq("-unchecked", "-deprecation")

val reactiveMongoVer = "0.12.6"
val silhouetteVersion = "5.0.1"

val commonSettings: Seq[Setting[_]] = Seq(
  scalaVersion := "2.12.3"
)


lazy val common = (project in file("./common")).
  settings(commonSettings).
  settings(
    resolvers += "Atlassian Maven Repository" at "https://maven.atlassian.com/content/repositories/atlassian-public/"
  ).
  settings(
    libraryDependencies ++= Seq(
      "com.mohiva" %% "play-silhouette" % "5.0.1" excludeAll (
        ExclusionRule(organization = "com.typesafe.play")
        ),

      "net.codingwell" %% "scala-guice" % "4.1.0",

      "com.typesafe.play" %% "play" % "2.6.6" % "provided",

      "org.reactivemongo" %% "reactivemongo" % "0.12.6",
      "org.reactivemongo" %% "play2-reactivemongo" % "0.12.6-play26",
      "org.reactivemongo" %% "reactivemongo-akkastream" % "0.12.6"
    )
  )


lazy val auth = (project in file("./auth")).
  enablePlugins(PlayScala).
  dependsOn(common).
  settings(commonSettings).

  settings(
    libraryDependencies ++= Seq(
      ws,

      "org.matthicks" %% "mailgun4s" % "1.0.6",

      "com.mohiva" %% "play-silhouette-password-bcrypt" % "5.0.1",
      "com.mohiva" %% "play-silhouette-persistence" % "5.0.1",
      "com.mohiva" %% "play-silhouette-crypto-jca" % "5.0.1",

      "net.codingwell" %% "scala-guice" % "4.1.0",
      "com.iheart" %% "ficus" % "1.4.2",

      "com.mohiva" %% "play-silhouette-testkit" % "5.0.1" % Test
    )
  )

lazy val user = (project in file("./user")).
  enablePlugins(PlayScala).
  dependsOn(common)

lazy val thank = (project in file("./thank")).
  enablePlugins(PlayScala).
  dependsOn(common)

lazy val payment = (project in file("./payment")).
  enablePlugins(PlayScala).
  dependsOn(common)

lazy val root = (project in file(".")).
  enablePlugins(PlayScala).
  enablePlugins(DockerPlugin).
  dependsOn(common, auth, payment, thank, user).
  aggregate(common, auth, payment, thank, user)


libraryDependencies ++= Seq(
  guice,
  ws,

  "net.codingwell" %% "scala-guice" % "4.1.0",

  "com.iheart" %% "ficus" % "1.4.2",

  "com.mohiva" %% "play-silhouette-password-bcrypt" % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-crypto-jca" % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-persistence" % silhouetteVersion,

  "io.sentry" % "sentry-logback" % "1.5.4",

  "org.apache.commons" % "commons-text" % "1.1" % Test,
  specs2 % Test
)

TwirlKeys.templateImports := Seq()

RoutesKeys.routesImport += "com.clemble.loveit.payment.controller._"

def readProperties(fileName: String): Map[String, String] = {
  val f = new File(fileName)
  if (!f.exists()) {
    return Map.empty[String, String]
  }

  Source.fromFile(f).
    getLines().
    map(_.trim()).
    filterNot(_.isEmpty).
    map(line => {
      val parts = line.split("=")
      (parts(0).trim(), parts(1).trim())
    }).
    toList.
    toMap[String, String]
}

envVars ++= readProperties("./local.properties")

testOptions in Test += Tests.Argument(TestFrameworks.Specs2, "console", "junitxml")
coverageExcludedFiles := """.*\.template\.scala;.*JavaScriptReverseRoutes.*;.*ReverseRoutes.*;.*Routes.*;.*Module.*;"""

javaOptions in Universal ++= Seq(
  "-Dpidfile.path=/dev/null"
)

javaOptions in Test ++= Seq("-Dconfig.resource=application.test.conf")

version in Docker := "latest"
maintainer in Docker := "antono@loveit.tips"
dockerBaseImage := "openjdk:8u131-jre"
dockerRepository := Some("loveit")
dockerExposedPorts := Seq(9000, 9443)

