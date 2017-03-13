package com.github.scw1109.servant

import ch.qos.logback.classic.{Level, LoggerContext}
import com.github.scw1109.servant.util.Helper
import com.typesafe.config.ConfigFactory
import org.asynchttpclient.DefaultAsyncHttpClient
import org.asynchttpclient.ws.{WebSocket, WebSocketTextListener, WebSocketUpgradeHandler}
import org.slf4j.LoggerFactory
import spark.Spark
import spark.Spark.get

import scala.io.StdIn
import scala.util.Try

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

  val config = ConfigFactory.load("servant")
  val port = config.getInt("servant.port")

  Servant.start(config, port)
  get("/status-check", (_, _) => "OK")

  if (devMode) {
    Spark.awaitInitialization()

    var webSocketRef = None: Option[WebSocket]

    val asyncHttpClient = new DefaultAsyncHttpClient()
    asyncHttpClient.prepareGet(s"ws://localhost:$port/websocket")
      .execute(new WebSocketUpgradeHandler.Builder().addWebSocketListener(
        new WebSocketTextListener {
          override def onMessage(message: String): Unit = {
            System.out.println(
              "-----\n" +
                s"Servant:\n$message\n" +
                "-----\n")
          }

          override def onOpen(websocket: WebSocket): Unit = {
            webSocketRef = Some(websocket)
          }

          override def onError(t: Throwable): Unit = {}

          override def onClose(websocket: WebSocket): Unit = {}
        }
      ).build())

    Try {
      while (true) {
        val line = StdIn.readLine()
        webSocketRef match {
          case Some(webSocket) =>
            webSocket.sendMessage(line)
          case None =>
            System.out.println("Websocket not ready")
        }
      }
    }
  }
}
