import sbt._
import sbt.Keys._

lazy val commonSettings = Seq(
  name := "hulk",
  organization := "io.github.reneweb",
  version := "0.1.1",
  scalaVersion := "2.11.7"
)

lazy val noPublishSettings = Seq(
  publish := (),
  publishLocal := (),
  bintrayRelease := (),
  publishArtifact := false
)

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishArtifact := true,
  publishArtifact in Test := false,

  bintrayReleaseOnPublish := false,
  bintrayPackage := name.value,
  bintrayOrganization in bintray := None,

  licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
  homepage := Some(url("https://github.com/reneweb/hulk")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/reneweb/hulk"),
      "scm:git:git@github.com:reneweb/hulk.git"
    )
  )
)

lazy val root = (project in file("."))
  .settings(noPublishSettings)
  .aggregate(framework, examples)

lazy val framework = project
  .settings(commonSettings)
  .settings(publishSettings)
  .settings(moduleName := "hulk-framework")
  .settings(libraryDependencies ++= Seq (
    "com.typesafe.akka" % "akka-http-experimental_2.11" % "2.4.2",
    "org.slf4j" % "slf4j-api" % "1.7.13",
    "ch.qos.logback" % "logback-classic" % "1.1.3",
    "com.codahale.metrics" % "metrics-core" % "3.0.2",
    "net.sf.ehcache" % "ehcache" % "2.10.1",
    "org.spire-math" % "cats-core_2.11" % "0.3.0",
    "com.github.spullara.mustache.java" % "compiler" % "0.9.1",

    "com.typesafe.play" % "play-json_2.11" % "2.5.0-M1",
    "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.5",

    "org.specs2" % "specs2_2.11" % "3.7" % "test",
    "com.typesafe.akka" % "akka-stream-testkit-experimental_2.11" % "2.0.3" % "test"
  ))

lazy val examples = project
  .settings(commonSettings)
  .settings(noPublishSettings)
  .settings(moduleName := "hulk-examples")
  .settings(coverageExcludedPackages := "hulk\\..*")
  .dependsOn(framework)