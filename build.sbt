name := "thanks-backend"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

val reactiveMongoVer = "0.12.1"

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "reactivemongo" % reactiveMongoVer,
  "org.reactivemongo" %% "play2-reactivemongo" % reactiveMongoVer,
  "org.reactivemongo" %% "reactivemongo-iteratees" % reactiveMongoVer,

  specs2 % Test
)

testOptions in Test += Tests.Argument(TestFrameworks.Specs2, "console", "junitxml")

coverageExcludedPackages := """controllers\..*Reverse.*;router.Routes.*;"""

enablePlugins(DockerPlugin)

