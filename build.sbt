import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.7",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "sbt-kafka-test",
    libraryDependencies += scalaTest % Test,
    libraryDependencies += "junit" % "junit" % "4.11" % "test",
    libraryDependencies += "org.apache.kafka" % "kafka_2.12" % "1.0.0",
    libraryDependencies += "org.apache.curator" % "curator-test" % "2.8.0" % "test",
  )
