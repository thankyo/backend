import play.sbt.routes.RoutesKeys

scalaVersion := "2.12.3"

scalacOptions ++= Seq("-unchecked", "-deprecation")

libraryDependencies ++= Seq(
  ws,
  "com.mohiva" %% "play-silhouette-crypto-jca" % "5.0.1",
)