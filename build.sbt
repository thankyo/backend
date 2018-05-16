import play.sbt.PlayImport.ws
import play.sbt.routes.RoutesKeys
import sbt.Keys.{libraryDependencies, resolvers}

import scala.io.Source

name := "backend"

version := "1.0-SNAPSHOT"

scalaVersion := "2.12.6"
scalacOptions ++= Seq("-unchecked", "-deprecation")

val silhouetteVersion = "5.0.4"
val reactMongoVersion = "0.13.0"
val scalaGuiceVersion = "4.2.0"
val elastic4sVersion = "6.1.3"

resolvers += Resolver.mavenCentral
resolvers += "Atlassian Maven Repository" at "https://maven.atlassian.com/content/repositories/atlassian-public/"
resolvers += Resolver.sbtPluginRepo("releases")

lazy val root = (project in file(".")).
  enablePlugins(PlayScala).
  enablePlugins(DockerPlugin).
  settings(routesImport += "com.clemble.loveit.auth.controller._").
  settings(routesImport += "com.clemble.loveit.payment.controller._")


libraryDependencies ++= Seq(
  // common settings
  "net.codingwell" %% "scala-guice" % scalaGuiceVersion,

  // common
  ws,

  "net.minidev" % "json-smart" % "2.3",
  "com.mohiva" %% "play-silhouette" % silhouetteVersion excludeAll (
    ExclusionRule(organization = "com.typesafe.play")
  ),

  "com.typesafe.play" %% "play" % "2.6.13" % "provided",

  "org.reactivemongo" %% "reactivemongo" % reactMongoVersion,
  "org.reactivemongo" %% "play2-reactivemongo" % s"${reactMongoVersion}-play26",
  "org.reactivemongo" %% "reactivemongo-akkastream" % reactMongoVersion,

    // root
  // ws,
  guice,

  "com.iheart" %% "ficus" % "1.4.3",

  "com.mohiva" %% "play-silhouette-password-bcrypt" % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-crypto-jca" % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-persistence" % silhouetteVersion,

  "io.sentry" % "sentry-logback" % "1.7.3",

  "org.apache.commons" % "commons-text" % "1.2" % Test,
  specs2 % Test,

  // auth dependencies
  // ws,

  "com.github.scribejava" % "scribejava-apis" % "5.4.0",

  "org.matthicks" %% "mailgun4s" % "1.0.9",

  "com.typesafe.play" %% "play-json-joda" % "2.6.9",

  "com.mohiva" %% "play-silhouette-password-bcrypt" % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-persistence" % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-crypto-jca" % silhouetteVersion,

  "com.iheart" %% "ficus" % "1.4.3",

  // payment
  //ws,
  "com.stripe" % "stripe-java" % "5.33.3",
  "com.mohiva" %% "play-silhouette-crypto-jca" % silhouetteVersion,

  // thank
  ws,
  "com.mohiva" %% "play-silhouette-crypto-jca" % silhouetteVersion,
  "org.jsoup" % "jsoup" % "1.11.3",
)

TwirlKeys.templateImports := Seq()

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
dockerBaseImage := "openjdk:8u171-jre-slim"
dockerRepository := Some("loveit")
dockerExposedPorts := Seq(9000)

