package com.github.scw1109.servant.connector.facebook

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import com.github.scw1109.servant.connector.facebook.event.FacebookEvent
import com.github.scw1109.servant.connector.{Facebook, Receiver}
import com.github.scw1109.servant.util.Algorithms
import org.apache.commons.codec.binary.Hex
import org.json4s.native.JsonMethods.parse
import org.json4s.{DefaultFormats, Formats}
import spark.Spark.{get, post}
import spark.{Request, Response}

/**
  * @author scw1109
  */
class FacebookReceiver(facebook: Facebook)
  extends Receiver[Facebook](facebook) {

  get(s"/${facebook.id}", (request, response) => {
    handleSubscribe(request, response)
  })

  post(s"/${facebook.id}", (request, _) => {
    logger.trace(s"Received message: ${request.body()}")
    handleWebHook(request)
    ""
  })

  implicit lazy val formats: Formats = DefaultFormats

  private val hashAlgorithm: String = Algorithms.SHA1
  private val secretKeySpec: SecretKeySpec =
    new SecretKeySpec(facebook.appSecret.getBytes(), hashAlgorithm)

  private def handleSubscribe(request: Request, response: Response) = {
    if (request.queryParams("hub.mode") == "subscribe" &&
      request.queryParams("hub.verify_token") == facebook.verifyToken) {
      logger.info("Validation success of Facebook verify token.")
      request.queryParams("hub.challenge")
    } else {
      logger.info("Failed validation of Facebook verify token.")
      response.status(403)
      ""
    }
  }

  private def handleWebHook(request: Request): Unit = {
    val headerSignature = request.headers("X-Hub-Signature")

    if (headerSignature != null &&
      signatureValidation(facebook.appSecret,
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
            messaging =>
              propagateEvent(FacebookEventObject(
                facebook,
                messaging
              ))
          }
      }
    }
  }
}
