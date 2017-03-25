package com.github.scw1109.servant.connector.line

import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import akka.actor.ActorRef
import com.github.scw1109.servant.connector.Line
import com.github.scw1109.servant.connector.line.model.{LineEvent, LineFormats}
import org.json4s.Formats
import org.json4s.native.JsonMethods.parse
import org.slf4j.{Logger, LoggerFactory}
import spark.Request
import spark.Spark.post

/**
  * @author scw1109
  */
class LineConnector(lineConfig: Line,
                    lineActor: ActorRef) {

  post(s"/${lineConfig.id}", (request, _) => {
    handleWebHook(request)
    ""
  })

  implicit lazy val formats: Formats = LineFormats.format

  private val logger: Logger = LoggerFactory.getLogger(getClass)

  private val hashAlgorithm: String = "HmacSHA256"
  private val secretKeySpec: SecretKeySpec =
    new SecretKeySpec(lineConfig.channelSecret.getBytes(), hashAlgorithm)

  def handleWebHook(request: Request): Unit = {
    logger.trace(s"Received message: ${request.body()}")

    val headerSignature = request.headers("X-Line-Signature")

    if (headerSignature != null &&
      signatureValidation(lineConfig.channelSecret,
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
      event => lineActor ! event
    }
  }
}
