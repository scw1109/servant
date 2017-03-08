package com.github.scw1109.servant

import spark.Spark._

/**
  * @author scw1109
  */
object Summon extends App {
  port(7777)
  get("/", (_, _) => "Hello World !!")
}
