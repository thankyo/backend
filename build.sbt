name := "thanks-backend"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).
  enablePlugins(PlayScala).
  enablePlugins(DockerPlugin)

scalaVersion := "2.11.8"

val reactiveMongoVer = "0.12.1"

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "reactivemongo" % reactiveMongoVer,
  "org.reactivemongo" %% "play2-reactivemongo" % reactiveMongoVer,
  "org.reactivemongo" %% "reactivemongo-iteratees" % reactiveMongoVer,

  "net.codingwell" %% "scala-guice" % "4.1.0",

  "com.pauldijou" %% "jwt-play" % "0.12.0",
  "com.pauldijou" %% "jwt-play-json" % "0.12.0",

  "com.iheart" %% "ficus" % "1.2.6",

  "com.mohiva" %% "play-silhouette" % "4.0.0",
  "com.mohiva" %% "play-silhouette-password-bcrypt" % "4.0.0",
  "com.mohiva" %% "play-silhouette-crypto-jca" % "4.0.0",
  "com.mohiva" %% "play-silhouette-persistence" % "4.0.0",

  "com.mohiva" %% "play-silhouette-testkit" % "4.0.0" % "test",
  specs2 % Test
)

testOptions in Test += Tests.Argument(TestFrameworks.Specs2, "console", "junitxml")
coverageExcludedPackages := """controllers\..*Reverse.*;router.Routes.*;com\.clemble\.thank\.controller\.*Reverse.*;.*\.template\.scala"""
coverageExcludedFiles := """.*\.template\.scala;.*JavaScriptReverseRoutes.*"""

javaOptions in Universal ++= Seq(
  "-Dpidfile.path=/dev/null"
)
version in Docker := "latest"
maintainer in Docker := "antono@clemble.com"
dockerBaseImage := "java:8u111-jre"
dockerRepository := Some("mavarazy")
dockerExposedPorts := Seq(9000, 9443)

