package com.github.scw1109.servant.connector.hipchat

import akka.actor.ActorRef
import com.github.scw1109.servant.connector.Hipchat
import com.github.scw1109.servant.connector.hipchat.model.WebHookData
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods.parse
import org.slf4j.{Logger, LoggerFactory}
import spark.Request
import spark.Spark.post

/**
  * @author scw1109
  */
class HipchatConnector(hipchatConfig: Hipchat,
                       hipchatActor: ActorRef) {

  post(s"/${hipchatConfig.id}", (request, _) => {
    handleWebHook(request)
    ""
  })

  private implicit lazy val formats = DefaultFormats

  private val logger: Logger = LoggerFactory.getLogger(getClass)

  def handleWebHook(request: Request): Unit = {
    logger.trace(s"Received message: ${request.body()}")

    val webHookData = parse(request.body).extract[WebHookData]
    hipchatActor ! webHookData
  }
}
