import play.sbt.routes.RoutesKeys

import scala.io.Source

name := "thank-backend"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).
  enablePlugins(PlayScala).
  enablePlugins(DockerPlugin)

scalaVersion := "2.11.11"

val reactiveMongoVer = "0.12.3"

resolvers += "Atlassian Maven Repository" at "https://maven.atlassian.com/repository/public"

lazy val common = project

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "reactivemongo" % reactiveMongoVer,
  "org.reactivemongo" %% "play2-reactivemongo" % reactiveMongoVer,
  "org.reactivemongo" %% "reactivemongo-akkastream" % reactiveMongoVer,

  "net.codingwell" %% "scala-guice" % "4.1.0",

  "com.iheart" %% "ficus" % "1.4.1",
  "com.mohiva" %% "play-silhouette" % "4.0.0",
  "com.mohiva" %% "play-silhouette-password-bcrypt" % "4.0.0",
  "com.mohiva" %% "play-silhouette-crypto-jca" % "4.0.0",
  "com.mohiva" %% "play-silhouette-persistence" % "4.0.0",

  "io.sentry" % "sentry-logback" % "1.4.0",

  "com.stripe" % "stripe-java" % "5.1.0",

  "com.atlassian.jwt" % "jwt-core" % "1.6.2",
  "com.atlassian.jwt" % "jwt-api" % "1.6.2",

  specs2 % Test
)

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
coverageExcludedFiles := """.*\.template\.scala;.*JavaScriptReverseRoutes.*;.*ReverseRoutes.*;.*Routes.*;.*Module.*;.*TestSocialProvider.*"""

javaOptions in Universal ++= Seq(
  "-Dpidfile.path=/dev/null"
)

javaOptions in Test ++= Seq("-Dconfig.resource=application.test.conf")

version in Docker := "latest"
maintainer in Docker := "antono@clemble.com"
dockerBaseImage := "openjdk:8u131-jre"
dockerRepository := Some("mavarazy")
dockerExposedPorts := Seq(9000, 9443)

