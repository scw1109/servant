import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.github.scw1109",
      version := "0.1",
      scalaVersion := "2.12.1"
    )),
    name := "servant",
    libraryDependencies ++= dependencies,
    libraryDependencies ++= testDependencies
  )

enablePlugins(JavaAppPackaging)
