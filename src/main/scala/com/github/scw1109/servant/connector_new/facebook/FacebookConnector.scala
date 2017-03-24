package com.github.scw1109.servant.connector_new.facebook

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import akka.actor.ActorRef
import com.github.scw1109.servant.connector_new.FacebookConfig
import com.github.scw1109.servant.connector_new.facebook.model.FacebookEvent
import org.apache.commons.codec.binary.Hex
import org.json4s.native.JsonMethods.parse
import org.json4s.{DefaultFormats, Formats}
import org.slf4j.{Logger, LoggerFactory}
import spark.{Request, Response}
import spark.Spark.{get, post}

/**
  * @author scw1109
  */
class FacebookConnector(facebookConfig: FacebookConfig,
                       facebookActor: ActorRef) {

  get(s"/${facebookConfig.id}", (request, response) => {
    handleSubscribe(request, response)
  })

  post(s"/${facebookConfig.id}", (request, _) => {
    handleWebHook(request)
    ""
  })

  implicit lazy val formats: Formats = DefaultFormats

  private val logger: Logger = LoggerFactory.getLogger(getClass)

  private val hashAlgorithm: String = "HmacSHA1"
  private val secretKeySpec: SecretKeySpec =
    new SecretKeySpec(facebookConfig.appSecret.getBytes(), hashAlgorithm)

  private def handleSubscribe(request: Request, response: Response) = {
    if (request.queryParams("hub.mode") == "subscribe" &&
      request.queryParams("hub.verify_token") == facebookConfig.verifyToken) {
      logger.info("Validation success of Facebook verify token.")
      request.queryParams("hub.challenge")
    } else {
      logger.info("Failed validation of Facebook verify token.")
      response.status(403)
      ""
    }
  }

  private def handleWebHook(request: Request): Unit = {
    logger.trace(s"Received message: ${request.body()}")

    val headerSignature = request.headers("X-Hub-Signature")

    if (headerSignature != null &&
      signatureValidation(facebookConfig.appSecret,
        headerSignature,
        request.bodyAsBytes())) {

      handle(request.body())
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

  def handle(body: String): Unit = {
    val payload = parse(body)

    if ((payload \ "object").extract[String] == "page") {
      (payload \ "entry").children map {
        _.extract[FacebookEvent]
      } foreach {
        entry =>
          entry.messaging foreach {
            messaging => facebookActor ! messaging
          }
      }
    }
  }
}
