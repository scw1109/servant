package com.github.scw1109.servant

import ch.qos.logback.classic.{Level, LoggerContext}
import com.github.scw1109.servant.util.Helper
import org.slf4j.LoggerFactory

/**
  * @author scw1109
  */
object Summon extends App {

  val devMode = Helper.loadDevEnv()
  if (devMode) {
    val loggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
    loggerContext.getLogger(this.getClass.getPackage.getName)
      .setLevel(Level.TRACE)
  }

  Servant.start()
}
