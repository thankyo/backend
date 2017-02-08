name := """thanks-backend"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

val reactiveMongoVer = "0.12.1"

libraryDependencies ++= Seq(
  cache,
  ws,
  evolutions,

  "org.reactivemongo" %% "reactivemongo" % reactiveMongoVer,
  "org.reactivemongo" %% "play2-reactivemongo" % reactiveMongoVer,
  "org.reactivemongo" %% "reactivemongo-iteratees" % reactiveMongoVer,

  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)


