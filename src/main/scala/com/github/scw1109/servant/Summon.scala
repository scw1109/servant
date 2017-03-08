package com.github.scw1109.servant

import spark.Spark._

/**
  * @author scw1109
  */
object Summon extends App {
  port(args.length match {
    case 1 => args(0).toInt
    case _ => 7777
  })

  get("/", (_, _) => "Hello World !!")
}
