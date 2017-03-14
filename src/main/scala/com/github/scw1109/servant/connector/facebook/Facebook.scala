package com.github.scw1109.servant.connector.facebook

import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import com.github.scw1109.servant.Servant
import com.github.scw1109.servant.connector.Connector
import com.github.scw1109.servant.message.{EventSource, IncomingMessage, OutgoingMessage}
import com.typesafe.config.Config
import org.apache.commons.codec.binary.Hex
import org.asynchttpclient.{AsyncCompletionHandler, DefaultAsyncHttpClient, Response}
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods.{compact, parse, render}
import org.json4s.{DefaultFormats, Formats}
import org.slf4j.{Logger, LoggerFactory}
import spark.Request
import spark.Spark.{get, post}

/**
  * @author scw1109
  */
object Facebook extends Connector {

  implicit lazy val formats: Formats = DefaultFormats

  private val logger: Logger = LoggerFactory.getLogger(getClass)
  private val facebookUrl: String = "https://graph.facebook.com/v2.8/"

  private val asyncHttpClient = new DefaultAsyncHttpClient()

  private var appSecret: String = _
  private var pageAccessToken: String = _

  private val hashAlgorithm: String = "HmacSHA1"
  private var secretKeySpec: SecretKeySpec = _

  override def init(config: Config): Unit = {
    appSecret = config.getString("servant.facebook.app-secret")
    pageAccessToken = config.getString("servant.facebook.page-access-token")

    secretKeySpec = new SecretKeySpec(appSecret.getBytes(), hashAlgorithm)

    get("/facebook", (request, response) => {
      if (request.queryParams("hub.mode") == "subscribe" &&
        request.queryParams("hub.verify_token") == "servant_facebook_token_1616") {
        logger.info("Validation success of Facebook verify token.")
        request.queryParams("hub.challenge")
      } else {
        logger.info("Failed validation of Facebook verify token.")
        response.status(403)
        ""
      }
    })

    post("/facebook", (request, _) => {
      handleWebHook(request)
      ""
    })
  }

  def handleWebHook(request: Request): Unit = {
    val headerSignature = request.headers("X-Hub-Signature")

    if (headerSignature != null &&
      signatureValidation(appSecret,
        headerSignature,
        request.bodyAsBytes())) {

      process(request.body())
    }
  }

  def signatureValidation(channelSecret: String, headerSignature: String, content: Array[Byte]): Boolean = {
    val mac = Mac.getInstance(hashAlgorithm)
    mac.init(secretKeySpec)
    val generatedSignature = mac.doFinal(content)
    val result = Hex.encodeHex(generatedSignature).mkString

    val expected = if (headerSignature.startsWith("sha1=")) {
      headerSignature.substring(5)
    } else {
      headerSignature
    }

    result == expected
  }

  def process(body: String): Unit = {
    val payload = parse(body)

    if ((payload \ "object").extract[String] == "page") {
      (payload \ "entry").children map {
        _.extract[FacebookEvent]
      } foreach {
        entry =>
          entry.messaging foreach {
            messaging =>
              messaging.message match {
                case Some(message) =>
                  Servant.process(IncomingMessage(message.text, messaging))
                case None =>
              }
          }
      }
    }
  }

  override def sendResponse(outgoingMessage: OutgoingMessage, eventSource: EventSource): Unit = {
    val messaging = eventSource.asInstanceOf[Messaging]

    val body = compact(render(
      ("recipient" ->
        ("id" -> messaging.sender.id)) ~
        ("message" ->
          ("text" -> outgoingMessage.text))
    ))

    postToApi("me/messages", body)
  }

  private def postToApi(api: String, body: String): Unit = {
    asyncHttpClient
      .preparePost(s"$facebookUrl$api?access_token=$pageAccessToken")
      .setHeader("Content-type", "application/json")
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
