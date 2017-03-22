package com.github.scw1109.servant

import akka.actor.{Actor, Props}
import com.github.scw1109.servant.connector_new.ConnectorConfig
import com.github.scw1109.servant.connector_new.slack.SlackActor
import pureconfig.loadConfig
import spark.Spark.port

/**
  * @author scw1109
  */
class ServantActor extends Actor {

  override def preStart(): Unit = {
    val config = context.system.settings.config

    port(config.getInt("servant.server.port"))

    config.getString("servant.mode") match {
      case "development" =>
      //        WebSocket.init(config)
      //        WebSocketClient.start()
      case _ =>
    }

    config.getConfigList("servant.connectors")
      .forEach({
        c =>
          loadConfig[ConnectorConfig](c) match {
            case Right(connectorConfig) =>
              context.system.actorOf(
                Props(classOf[SlackActor], connectorConfig),
                connectorConfig.id)
            case Left(_) =>
          }
      })

//    HealthCheck.start(config)
  }

  override def receive: Receive = {
    case _ =>
  }
}
