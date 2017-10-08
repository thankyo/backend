scalaVersion := "2.12.3"

resolvers += "Atlassian Maven Repository" at "https://maven.atlassian.com/content/repositories/atlassian-public/"

scalacOptions ++= Seq("-unchecked", "-deprecation")

libraryDependencies ++= Seq(
  "com.mohiva" %% "play-silhouette" % "5.0.1",

  "net.codingwell" %% "scala-guice" % "4.1.0",

  "org.reactivemongo" %% "reactivemongo" % "0.12.6",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.12.6-play26",
  "org.reactivemongo" %% "reactivemongo-akkastream" % "0.12.6"
)
