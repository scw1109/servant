package com.github.scw1109.servant.rest

import com.typesafe.config.Config
import spark.Spark.get

/**
  * @author scw1109
  */
object HealthCheck {

  def start(config: Config): Unit = {
    get("/status-check", (_, _) => "OK")
  }
}
