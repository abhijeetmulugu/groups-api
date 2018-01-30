name := """groups-api"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.11"


libraryDependencies += cache
libraryDependencies += ws
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test
libraryDependencies ++=Seq(
  filters,
  "com.typesafe.play" %% "play-slick" % "3.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "3.0.0",
  "org.postgresql" % "postgresql" % "42.1.1",
  "com.typesafe.play" %% "play-mailer" % "5.0.0",
  "com.insuranceinbox" %% "deadbolt-handler" % "0.2.12",
  "com.typesafe.play.modules" %% "play-modules-redis" % "2.5.0"
)


