name := "scaladinext"

// common variables
val vaadinVersion = "7.5.2"

val scaladinVersion = "3.2-SNAPSHOT"

// common settings
lazy val commonSettings = Seq(
  scalaVersion := "2.11.7",
  organization := "scaladinext",
  version := "0.1.1",

  // basic dependencies
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,

    // vaadin libraries
    "com.vaadin" % "vaadin-server" % vaadinVersion,
    "com.vaadin" % "vaadin-push" % vaadinVersion,
    "com.vaadin" % "vaadin-client-compiled" % vaadinVersion,
    "com.vaadin" % "vaadin-themes" % vaadinVersion,
    "com.vaadin" % "vaadin-client" % vaadinVersion,

    "org.vaadin.addons" %% "scaladin" % scaladinVersion
  )
)

// defining projects
lazy val common = project.in(file("scaladinext-common")).settings(commonSettings: _*).settings(
  name := "common",
  libraryDependencies ++= Seq(
    "com.google.guava" % "guava" % "18.0", // for AppEvent
    "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
    "org.slf4j" % "slf4j-api" % "1.7.10",
    "ch.qos.logback" % "logback-core" % "1.1.2",
    "ch.qos.logback" % "logback-classic" % "1.1.2")


)
lazy val mongo = project.in(file("scaladinext-mongo")).settings(commonSettings: _*).settings(
  name := "mongo",
  libraryDependencies += "net.liftweb" %% "lift-mongodb-record" % "2.6"
)

lazy val reactive = project.in(file("scaladinext-reactive")).settings(commonSettings: _*).settings(
  name := "reactive",
  libraryDependencies += "com.lihaoyi" %% "scalarx" % "0.2.8"
)

