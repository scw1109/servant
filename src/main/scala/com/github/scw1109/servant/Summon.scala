package com.github.scw1109.servant

import com.typesafe.config.{Config, ConfigFactory}
import spark.Spark._

/**
  * @author scw1109
  */
object Summon extends App {
  val config: Config = ConfigFactory.load("servant")

  port(config.getInt("servant.port"))

  get("/", (_, _) => "Hello World !!")
}
