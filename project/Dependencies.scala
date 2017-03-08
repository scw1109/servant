import sbt._

object Dependencies {
  lazy val dependencies = Seq(
    "com.sparkjava" % "spark-core" % "2.5.5",
    "ch.qos.logback" % "logback-classic" % "1.2.1",
    "org.scalatest" %% "scalatest" % "3.0.1" % Test
  )
}
