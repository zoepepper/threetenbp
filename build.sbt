parallelExecution in ThisBuild := false

name := "threetenbp"
organization := "org.threeten"
scalaVersion := "2.11.7"
version := "1.3.3-SNAPSHOT"
isSnapshot := true

lazy val threetenbp = project.in(file("."))
  .settings(
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % "3.0.0-M15" % "test",
      "junit" % "junit" % "4.12" % "test",
      "org.testng" % "testng" % "6.8.8" % "test"
    ),
    scalacOptions ++= Seq("-Xexperimental")
  )

//lazy val threetenbpJVM = threetenbp.jvm
//
//lazy val threetenbpJS = threetenbp.js
