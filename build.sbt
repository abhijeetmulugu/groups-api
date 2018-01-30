name := """groups-api"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.11"

libraryDependencies += ws
libraryDependencies += guice
libraryDependencies += play.sbt.PlayImport.cacheApi
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.0" % Test
libraryDependencies ++=Seq(
  filters,
  "com.typesafe.play" %% "play-slick" % "3.0.1",
  "com.typesafe.play" %% "play-slick-evolutions" % "3.0.1",
  "org.postgresql" % "postgresql" % "42.1.4",
  "com.insuranceinbox" %% "deadbolt-handler" % "0.2.13"
)
libraryDependencies += "com.github.karelcemus" %% "play-redis" % "1.5.1"



