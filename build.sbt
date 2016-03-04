import org.scalajs.sbtplugin.cross.CrossProject

parallelExecution in ThisBuild := false

name := "threetenbp"
organization := "org.threeten"
version := "1.3.3-SNAPSHOT"
isSnapshot := true

lazy val commonSettings = Seq(
  scalacOptions ++= Seq("-Xexperimental"),
  scalaVersion := "2.11.7",
  crossScalaVersions := Seq("2.11.7"),
  publishArtifact in (Compile, packageDoc) := false,
  publishArtifact in packageDoc := false
)

lazy val threetenbpRoot = project.in(file("."))
  .aggregate(threetenbp, threetenbpJS)
  .settings(
    publish := {},
    publishLocal := {},
    crossScalaVersions := Seq("2.11.7")
  )


lazy val threetenbpCross = crossProject.crossType(CrossType.Full).in(file("."))
  .settings(
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % "3.0.0-M15" % "test",
      "junit" % "junit" % "4.12" % "test",
      "org.testng" % "testng" % "6.8.8" % "test"
    )
  )

lazy val threetenbp = threetenbpCross.jvm
  .settings(commonSettings: _*)
lazy val threetenbpJS = threetenbpCross.js
  .settings(commonSettings: _*)
