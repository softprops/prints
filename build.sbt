organization := "me.lessis"

name := "prints"

version := "0.1.0-SNAPSHOT"

crossScalaVersions := Seq("2.10.4", "2.11.5")

scalaVersion := crossScalaVersions.value.last

resolvers += "softprops-maven" at "http://dl.bintray.com/content/softprops/maven"

libraryDependencies ++= Seq(
  "me.lessis" %% "base64" % "0.2.0",
  "org.json4s" %% "json4s-native" % "3.2.11",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test"
)
