import play.sbt.routes.RoutesKeys

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  ws,
  "com.stripe" % "stripe-java" % "5.10.0",
  "com.mohiva" %% "play-silhouette-crypto-jca" % "5.0.1",
)

RoutesKeys.routesImport += "com.clemble.loveit.payment.controller._"