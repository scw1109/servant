package com.github.scw1109.servant

import akka.actor.{ActorSystem, Props}
import ch.qos.logback.classic.{Level, LoggerContext}
import com.github.scw1109.servant.client.WebSocketClient
import com.github.scw1109.servant.rest.HealthCheck
import com.github.scw1109.servant.util.Helper
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import spark.Spark

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

  val servant = ActorSystem("servant")
  servant.actorOf(Props[ServantActor], "servant")

//  val config = ConfigFactory.load()
//  val port = config.getInt("servant.server.port")

//  Servant.start(config, port)
//  HealthCheck.start(config)
//
//  if (devMode) {
//    Spark.awaitInitialization()
//    WebSocketClient.start(port)
//  }
}
