package com.github.scw1109.servant

import akka.actor.{ActorSystem, Props}
import ch.qos.logback.classic.{Level, LoggerContext}
import com.github.scw1109.servant.core.ServantActor
import com.github.scw1109.servant.util.Helpers
import org.slf4j.LoggerFactory

/**
  * @author scw1109
  */
object Summon extends App {

  val devMode = Helpers.loadDevEnv()
  if (devMode) {
    val loggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
    loggerContext.getLogger(this.getClass.getPackage.getName)
      .setLevel(Level.TRACE)
  }

  val logger = LoggerFactory.getLogger(getClass)
  logger.info("Starting Servant system ... ")

  val servant = ActorSystem("servant")
  servant.actorOf(Props[ServantActor], "servant")
}
