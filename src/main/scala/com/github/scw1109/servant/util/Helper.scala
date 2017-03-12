package com.github.scw1109.servant.util

import java.nio.file.{Files, Paths}
import java.util.Properties

/**
  * @author scw1109
  */
object Helper {

  def loadDevEnv(): Unit = {
    val path = Paths.get("dev.secret")
    if (Files.exists(path)) {
      val props = new Properties()
      props.load(Files.newInputStream(path))
      props.forEach((k, v) => {
        System.setProperty(k.toString, v.toString)
      })
    }
  }
}