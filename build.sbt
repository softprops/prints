organization := "me.lessis"

name := "prints"

version := "0.1.0-SNAPSHOT"

crossScalaVersions := Seq("2.10.5", "2.11.7")

scalaVersion := crossScalaVersions.value.last

resolvers += "softprops-maven" at "http://dl.bintray.com/content/softprops/maven"

libraryDependencies ++= Seq(
  "me.lessis" %% "base64" % "0.2.0",
  "org.json4s" %% "json4s-native" % "3.2.11",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test"
)

licenses := Seq(
  ("MIT", url(s"https://github.com/softprops/${name.value}/blob/${version.value}/LICENSE")))

bintrayPackageLabels := Seq("jwt")

pomExtra := (
  <scm>
    <url>git@github.com:softprops/${name.value}.git</url>
    <connection>scm:git:git@github.com:softprops/{name.value}.git</connection>
  </scm>
  <developers>
    <developer>
      <id>softprops</id>
      <name>Doug Tangren</name>
      <url>https://github.com/softprops</url>
    </developer>
  </developers>)
