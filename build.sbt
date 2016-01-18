import sbt._
import sbt.Keys._

lazy val commonSettings = Seq(
  name := "hulk",
  version := "0.1.0",
  scalaVersion := "2.11.7",
  crossScalaVersions := Seq("2.10.6", "2.11.7")
)

lazy val root = (project in file("."))
  .aggregate(framework, examples)

lazy val framework = project
  .settings(commonSettings: _*)
  .settings(moduleName := "framework")
  .settings(libraryDependencies ++= Seq (
    "com.typesafe.akka" % "akka-http-experimental_2.11" % "2.0.2",
    "org.slf4j" % "slf4j-api" % "1.7.13",
    "ch.qos.logback" % "logback-classic" % "1.1.3",
    "com.codahale.metrics" % "metrics-core" % "3.0.2",
    "net.sf.ehcache" % "ehcache" % "2.10.1",
    "org.spire-math" % "cats-core_2.11" % "0.3.0",

    "com.typesafe.play" % "play-json_2.11" % "2.5.0-M1",
    "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.5"
  ))

lazy val examples = project
  .settings(commonSettings: _*)
  .settings(moduleName := "examples")
  .dependsOn(framework)