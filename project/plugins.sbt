// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.5")

// Coverage plugins
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.0")
addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.1.0")

// Use latest libraries
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.3.0")

resolvers += "Atlassian Maven Repository" at "https://maven.atlassian.com/repository/public"