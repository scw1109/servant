package com.github.scw1109.servant.connector.slack.rtm

import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{Executors, ScheduledExecutorService, ScheduledFuture, TimeUnit}

import com.github.scw1109.servant.connector.Connector
import com.github.scw1109.servant.message.{EventSource, OutgoingMessage}
import com.typesafe.config.Config
import org.asynchttpclient.ws.{WebSocket, WebSocketTextListener, WebSocketUpgradeHandler}
import org.asynchttpclient.{AsyncCompletionHandler, AsyncHttpClient, DefaultAsyncHttpClient, Response}
import org.json4s.DefaultFormats
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods.{compact, parse, render}
import org.slf4j.{Logger, LoggerFactory}

/**
  * @author scw1109
  */
object SlackRtm extends Connector {

  implicit lazy val formats = DefaultFormats

  private val logger: Logger = LoggerFactory.getLogger(getClass)
  private val slackUrl: String = "https://slack.com/api/"

  private val asyncHttpClient: AsyncHttpClient = new DefaultAsyncHttpClient()
  private val scheduledExecutors: ScheduledExecutorService = Executors.newScheduledThreadPool(5)

  private var botOauthToken: String = _

  override def init(config: Config): Unit = {
    botOauthToken = config.getString("servant.slack.bot-oauth-token")

    rtmStart()
  }

  def rtmStart(): Unit = {
    val api = "rtm.start"
    asyncHttpClient
      .preparePost(slackUrl + api)
      .setBody(s"token=$botOauthToken")
      .setHeader("Content-type", "application/x-www-form-urlencoded")
      .execute(new AsyncCompletionHandler[Unit] {
        override def onCompleted(response: Response): Unit = {
          if (response.getStatusCode == 200) {
            logger.info(s"Successfully get info from $api")
            val body = response.getResponseBody(StandardCharsets.UTF_8)
            logger.trace(body)
            val rtmResponse = parse(body).extract[RtmResponse]
            startWebSocket(rtmResponse.url)
          } else {
            logger.warn(s"Failed to get slack info from $api\n" +
              s"response status is ${response.getStatusCode} ${response.getStatusText}")
          }
        }
      })
  }

  def startWebSocket(url: String): Unit = {
    asyncHttpClient.prepareGet(url)
      .execute(new WebSocketUpgradeHandler.Builder()
        .addWebSocketListener(
          new WebSocketTextListener {
            var currentWebSocket: Option[WebSocket] = None: Option[WebSocket]
            var scheduledFuture: Option[ScheduledFuture[_]] = None: Option[ScheduledFuture[_]]
            val messageIdCounter = new AtomicInteger(0)

            override def onOpen(websocket: WebSocket): Unit = {
              currentWebSocket = Some(websocket)
              scheduledFuture = Option(
                scheduledExecutors.scheduleAtFixedRate(
                  () => {
                    val payload = compact(render(
                      ("id" -> messageIdCounter.incrementAndGet()) ~
                        ("type" -> "ping") ~
                        ("time" -> System.currentTimeMillis())
                    ))

                    currentWebSocket match {
                      case Some(ws) =>
                        if (ws.isOpen)
                          ws.sendMessage(payload)
                      case None =>
                    }
                  }, 0, 30, TimeUnit.SECONDS)
              )
            }

            override def onClose(websocket: WebSocket): Unit = {
              currentWebSocket = None: Option[WebSocket]

              scheduledFuture match {
                case Some(future) =>
                  future.cancel(false)
                case None =>
              }

              rtmStart()
            }

            override def onMessage(message: String): Unit = {
              System.out.println(message)
            }

            override def onError(t: Throwable): Unit = {
              logger.debug("WebSocket error of Slack RTM: $t")
            }
          }
        ).build())
  }

  override def sendResponse(outgoingMessage: OutgoingMessage, eventSource: EventSource): Unit = {

  }
}
