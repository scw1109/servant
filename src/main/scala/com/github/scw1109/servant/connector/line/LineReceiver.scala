package com.github.scw1109.servant.connector.line

import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import akka.actor.ActorRef
import com.github.scw1109.servant.connector.line.event.{LineEvent, LineFormats, MessageEvent}
import com.github.scw1109.servant.connector.{Line, Receiver}
import com.github.scw1109.servant.util.Algorithms
import org.json4s.Formats
import org.json4s.native.JsonMethods.parse
import spark.Request
import spark.Spark.post

/**
  * @author scw1109
  */
class LineReceiver(line: Line, lineActor: ActorRef)
  extends Receiver[Line](line) {

  post(s"/${line.id}", (request, _) => {
    logger.trace(s"Received message: ${request.body()}")
    handleWebHook(request)
    ""
  })

  implicit lazy val formats: Formats = LineFormats.format

  private val hashAlgorithm: String = Algorithms.SHA256
  private val secretKeySpec: SecretKeySpec =
    new SecretKeySpec(line.channelSecret.getBytes(), hashAlgorithm)

  def handleWebHook(request: Request): Unit = {
    val headerSignature = request.headers("X-Line-Signature")

    if (headerSignature != null &&
      signatureValidation(line.channelSecret,
        headerSignature,
        request.bodyAsBytes())) {

      handle(request.body())
    }
  }

  def signatureValidation(channelSecret: String,
                          headerSignature: String,
                          content: Array[Byte]): Boolean = {

    val mac = Mac.getInstance(hashAlgorithm)
    mac.init(secretKeySpec)
    val generatedSignature = mac.doFinal(content)
    val decodedHeaderSignature = Base64.getDecoder.decode(headerSignature)
    MessageDigest.isEqual(decodedHeaderSignature, generatedSignature)
  }

  def handle(body: String): Unit = {
    (parse(body) \ "events").children map {
      _.extract[LineEvent]
    } foreach {
      case messageEvent: MessageEvent =>
        propagateEvent(LineEventObject(
          line,
          messageEvent
        ))
      case _ =>
    }
  }
}
