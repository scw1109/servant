package com.github.scw1109.servant

import akka.actor.{Actor, Props}
import com.github.scw1109.servant.client.WebSocketClient
import com.github.scw1109.servant.connector_new.{ConnectorConfig, WebSocketEnabled}
import pureconfig.loadConfig
import spark.Spark.port

import scala.collection.JavaConverters._


/**
  * @author scw1109
  */
class ServantActor extends Actor {

  override def preStart(): Unit = {
    val config = context.system.settings.config

    val serverPort = config.getInt("servant.server.port")
    port(serverPort)

    val connectors: Seq[Option[ConnectorConfig]] = config
      .getConfigList("servant.connectors").asScala
      .map {
        c =>
          loadConfig[ConnectorConfig](c) match {
            case Right(connectorConfig) =>
              Some(connectorConfig)
            case Left(_) => None
          }
      }

    val wsConnectors = connectors.filter(c =>
      c.isDefined && c.get.isInstanceOf[WebSocketEnabled])
    wsConnectors.foreach(c => startConnector(c))

    val nonWsConnectors = connectors.filter(c =>
      c.isDefined && !c.get.isInstanceOf[WebSocketEnabled])
    nonWsConnectors.foreach(c => startConnector(c))

    val clientConfig = config.getConfig("servant.client")
    if (clientConfig.getBoolean("enable")) {
      WebSocketClient.start(serverPort, clientConfig.getString("path"))
    }

    //    HealthCheck.start(config)
  }

  private def startConnector(c: Option[ConnectorConfig]) = {
    val connector = c.get
    context.system.actorOf(
      Props(connector.actorType, connector),
      connector.id)
  }

  override def receive: Receive = {
    case _ =>
  }
}
