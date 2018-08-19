name := """tasks-backend"""
organization := "com.ryanbern"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.6"

libraryDependencies ++= Seq(
  guice,
  jdbc,
  "org.postgresql" % "postgresql" % "9.4-1200-jdbc41",
  "com.typesafe.play" %% "anorm" % "2.5.3",
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "com.typesafe.play" %% "play-json-joda" % "2.6.10" excludeAll(ExclusionRule(organization = "org.slf4j")),
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
)

