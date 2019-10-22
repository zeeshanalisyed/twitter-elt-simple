import Dependencies._

ThisBuild / scalaVersion     := "2.11.0"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "org.examples"
ThisBuild / organizationName := "humandata"

lazy val root = (project in file("."))
  .settings(
    name := "twitter-elt-simple",
    libraryDependencies += scalaTest % Test
  )

// https://mvnrepository.com/artifact/log4j/log4j
libraryDependencies += "log4j" % "log4j" % "1.2.17"

// https://mvnrepository.com/artifact/org.apache.spark/spark-core
libraryDependencies += "org.apache.spark" % "spark-core_2.11" % "2.2.0"

// https://mvnrepository.com/artifact/org.apache.spark/spark-sql
libraryDependencies += "org.apache.spark" % "spark-sql_2.11" % "2.2.0"

// https://mvnrepository.com/artifact/com.danielasfregola/twitter4s
libraryDependencies += "com.danielasfregola" % "twitter4s_2.11" % "6.1"

// https://mvnrepository.com/artifact/edu.stanford.nlp/stanford-corenlp
libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "3.9.2"


// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
