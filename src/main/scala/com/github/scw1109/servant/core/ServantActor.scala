package com.github.scw1109.servant.core

import akka.actor.Props
import com.github.scw1109.servant.client.Client
import com.github.scw1109.servant.connector.{Connector, WebSocketEnabled}
import com.github.scw1109.servant.webservice.WebService
import com.typesafe.config.Config
import pureconfig.loadConfig
import spark.Spark
import spark.Spark.port

import scala.collection.JavaConverters._

/**
  * @author scw1109
  */
class ServantActor extends ServantBaseActor {

  override def preStart(): Unit = {
    val config = context.system.settings.config

    val serverPort = config.getInt("servant.server.port")
    port(serverPort)

    startConnectors(config)
    startWebServices(config)

    Spark.init()
    Spark.awaitInitialization()

    startClients(config)
  }

  private def startConnectors(config: Config) = {
    logger.trace("Starting connectors ...")

    val connectors: Seq[Connector] = config
      .getConfigList("servant.connectors").asScala
      .map {
        c =>
          loadConfig[Connector](c) match {
            case Right(connector) =>
              Option(connector)
            case Left(_) => None
          }
      }.filter(_.isDefined).map(_.get)

    val startConnector = {
      (connector: Connector) =>
        logger.trace(s"Starting connector [${connector.id}]")
        context.system.actorOf(
          Props(connector.actorType, connector),
          s"connector-${connector.id}")
    }

    // Must start all websocket connectors before others
    val wsConnectors = connectors.filter(_.isInstanceOf[WebSocketEnabled])
    wsConnectors.foreach(c => startConnector(c))

    // Sleep 1s to allow websocket connectors starts
    Thread.sleep(1000)

    val nonWsConnectors = connectors.filter(!_.isInstanceOf[WebSocketEnabled])
    nonWsConnectors.foreach(c => startConnector(c))
  }

  private def startWebServices(config: Config) = {
    logger.trace(s"Starting webservices ...")

    config.getConfigList("servant.webservices").asScala
      .map {
        ws =>
          loadConfig[WebService](ws) match {
            case Right(webService) =>
              logger.trace(s"Starting webservice [${webService.id}]")
              context.system.actorOf(
                Props(webService.actorType, webService),
                s"webservice-${webService.id}")
            case Left(_) =>
          }
      }
  }

  private def startClients(config: Config) = {
    logger.trace(s"Starting clients ...")

    config.getConfigList("servant.clients").asScala
      .map {
        c =>
          loadConfig[Client](c) match {
            case Right(client) =>
              logger.trace(s"Starting client [${client.id}]")
              context.system.actorOf(
                Props(client.actorType, client),
                s"client-${client.id}")
            case Left(_) =>
          }
      }
  }

  override def receive: Receive = {
    case _ =>
  }
}
