package com.github.scw1109.servant.connector.hipchat

import akka.actor.ActorRef
import com.github.scw1109.servant.connector.hipchat.event.WebHookData
import com.github.scw1109.servant.connector.{Hipchat, Receiver}
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods.parse
import spark.Request
import spark.Spark.post

/**
  * @author scw1109
  */
class HipchatReceiver(hipchat: Hipchat, hipchatActor: ActorRef)
  extends Receiver[Hipchat](hipchat) {

  post(s"/${hipchat.id}", (request, _) => {
    logger.trace(s"Received message: ${request.body()}")
    handleWebHook(request)
    ""
  })

  private implicit lazy val formats = DefaultFormats

  def handleWebHook(request: Request): Unit = {
    val webHookData = parse(request.body).extract[WebHookData]
    propagateEvent(HipchatEventObject(
      hipchat,
      webHookData
    ))
  }
}
