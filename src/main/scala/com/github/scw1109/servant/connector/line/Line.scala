package com.github.scw1109.servant.connector.line

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import com.github.scw1109.servant.Servant
import com.github.scw1109.servant.connector.Connector
import com.github.scw1109.servant.message.{EventSource, IncomingMessage, OutgoingMessage}
import com.typesafe.config.Config
import org.asynchttpclient.{AsyncCompletionHandler, DefaultAsyncHttpClient, Response}
import org.json4s.Formats
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods.{compact, parse, render}
import org.slf4j.{Logger, LoggerFactory}
import spark.Request
import spark.Spark.post

/**
  * @author scw1109
  */
object Line extends Connector {

  implicit lazy val formats: Formats = LineFormats.format

  private val logger: Logger = LoggerFactory.getLogger(getClass)
  private val lineUrl: String = "https://api.line.me/v2/"

  private val asyncHttpClient = new DefaultAsyncHttpClient()

  private var channelSecret: String = _
  private var channelAccessToken: String = _
  private var botUserId: String = _

  private val hashAlgorithm: String = "HmacSHA256"
  private var secretKeySpec: SecretKeySpec = _

  override def init(config: Config): Unit = {
    channelSecret = config.getString("servant.line.channel-secret")
    channelAccessToken = config.getString("servant.line.channel-access-token")
    botUserId = config.getString("servant.line.bot-user-id")

    secretKeySpec = new SecretKeySpec(channelSecret.getBytes(), hashAlgorithm)

    post("/line", (request, _) => {
      handleWebHook(request)
      ""
    })
  }

  def handleWebHook(request: Request): Any = {
    val headerSignature = request.headers("X-Line-Signature")

    if (headerSignature != null &&
      signatureValidation(channelSecret,
        headerSignature,
        request.bodyAsBytes())) {

      process(request.body())
      ""
    }
  }

  def signatureValidation(channelSecret: String, headerSignature: String, content: Array[Byte]): Boolean = {
    val mac = Mac.getInstance(hashAlgorithm)
    mac.init(secretKeySpec)
    val generatedSignature = mac.doFinal(content)
    val decodedHeaderSignature = Base64.getDecoder.decode(headerSignature)
    MessageDigest.isEqual(decodedHeaderSignature, generatedSignature)
  }

  def process(body: String): Unit = {
    (parse(body) \ "events").children map {
      _.extract[LineEvent]
    } foreach {
      case event@MessageEvent(_, _, _, _, message) =>
        message match {
          case TextMessage(_, _, text) =>
            Servant.process(IncomingMessage(text, event))
        }
      case _ =>
    }
  }

  override def sendResponse(outgoingMessage: OutgoingMessage,
                            eventSource: EventSource): Unit = {

    val receiverId = eventSource.asInstanceOf[MessageEvent].source match {
      case User(_, userId) => userId
      case Group(_, groupId) => groupId
      case Room(_, roomId) => roomId
    }

    val body = compact(render(
      ("to" -> receiverId) ~
        ("messages" ->
          List(
            ("type", "text") ~
              ("text", outgoingMessage.text)
          ))
    ))

    postToApi("bot/message/push", body)
  }

  private def postToApi(api: String, body: String): Unit = {
    asyncHttpClient
      .preparePost(lineUrl + api)
      .setHeader("Content-type", "application/json")
      .setHeader("Authorization", s"Bearer $channelAccessToken")
      .setBody(body.getBytes(StandardCharsets.UTF_8))
      .execute(new AsyncCompletionHandler[Unit] {
        override def onCompleted(response: Response): Unit = {
          if (response.getStatusCode != 200) {
            logger.warn(s"Failed to send message to $api.\n" +
              s"response status is ${response.getStatusCode} ${response.getStatusText}")
          }
        }
      })
  }
}
