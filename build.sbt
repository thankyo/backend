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

  specs2 % Test
)

testOptions in Test += Tests.Argument(TestFrameworks.Specs2, "console", "junitxml")
coverageExcludedPackages := """controllers\..*Reverse.*;router.Routes.*;com\.clemble\.thank\.controller\.*Reverse.*"""

javaOptions in Universal ++= Seq(
  "-Dpidfile.path=/dev/null"
)
version in Docker := "latest"
maintainer in Docker := "antono@clemble.com"
dockerBaseImage := "java:8u111-jre"
dockerRepository := Some("mavarazy")

