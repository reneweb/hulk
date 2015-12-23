name := "coast"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq (
  "com.typesafe.akka" % "akka-http-experimental_2.11" % "2.0",

  "io.circe" %% "circe-core" % "0.2.1",
  "io.circe" %% "circe-generic" % "0.2.1",
  "io.circe" %% "circe-parse" % "0.2.1",
  "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.5"
)
    