scalaVersion := "2.13.12"
name := "BigLosses"
organization := "ch.epfl.scala"
version := "2.0"

val slf4jVersion = "2.0.0-alpha5"
val kafka_streams_scala_version = "0.2.1"
val catsVersion = "3.4.8"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-parser-combinators" % "2.3.0",
  "org.apache.kafka" % "kafka-clients" % "2.8.0",
  "org.apache.kafka" % "kafka-streams" % "2.8.0",
  "org.apache.kafka" %% "kafka-streams-scala" % "2.8.0",
  "io.circe" %% "circe-core" % "0.14.1",
  "io.circe" %% "circe-generic" % "0.14.1",
  "io.circe" %% "circe-parser" % "0.14.1",
  "io.circe" %% "circe-config" % "0.8.0",
  "org.slf4j" % "slf4j-api" % slf4jVersion,
  "org.slf4j" % "slf4j-simple" % slf4jVersion,
  "org.typelevel" %% "cats-effect" % catsVersion,
  "org.tpolecat" %% "skunk-core" % "0.5.1",
  "com.typesafe" % "config" % "1.4.3"
)

assemblyMergeStrategy in assembly := {
 case PathList("META-INF", xs @ _*) => MergeStrategy.discard
 case x => MergeStrategy.first
}

