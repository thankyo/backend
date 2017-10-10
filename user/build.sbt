import play.sbt.routes.RoutesKeys

scalaVersion := "2.12.3"

scalacOptions ++= Seq("-unchecked", "-deprecation")

libraryDependencies ++= Seq(
  ws
)