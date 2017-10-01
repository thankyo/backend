scalaVersion := "2.12.3"

libraryDependencies ++= Seq(

  "com.mohiva" %% "play-silhouette-password-bcrypt" % "5.0.1",
  "com.mohiva" %% "play-silhouette-persistence" % "5.0.1",
  "com.mohiva" %% "play-silhouette-crypto-jca" % "5.0.1",

  "net.codingwell" %% "scala-guice" % "4.1.0",
  "com.iheart" %% "ficus" % "1.4.2",

  "com.typesafe.play" %% "play-mailer" % "6.0.1",
  "com.typesafe.play" %% "play-mailer-guice" % "6.0.1",

  "com.adrianhurt" %% "play-bootstrap" % "1.2-P26-B3",
  "com.mohiva" %% "play-silhouette-testkit" % "5.0.1" % Test
)
