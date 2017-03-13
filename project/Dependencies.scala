import sbt._

object Dependencies {
  lazy val dependencies = Seq(
    "com.sparkjava" % "spark-core" % "2.5.5",
    "ch.qos.logback" % "logback-classic" % "1.2.1",
    "com.typesafe" % "config" % "1.3.1",
    "org.json4s" %% "json4s-native" % "3.5.0",
    "org.asynchttpclient" % "async-http-client" % "2.0.30",
    "org.jsoup" % "jsoup" % "1.10.2"
  )

  lazy val testDependencies = Seq(
    "org.scalatest" %% "scalatest" % "3.0.1" % Test
  )
}
