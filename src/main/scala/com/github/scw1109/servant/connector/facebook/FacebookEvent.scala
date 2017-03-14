package com.github.scw1109.servant.connector.facebook

import com.github.scw1109.servant.message.{EventSource, EventSourceType, FacebookType}
import org.json4s.JsonAST.JObject

/**
  * @author scw1109
  */
case class FacebookEvent(id: String, time: Long,
                         messaging: List[Messaging]) {
}

case class Messaging(sender: Sender, recipient: Recipient,
                     timestamp: Long, message: Option[Message],
                     postback: Option[Postback]) extends EventSource {

  override def getType: EventSourceType = FacebookType()

}

case class Sender(id: String)

case class Recipient(id: String)

case class Message(mid: String, text: String, attachments: List[Attachment],
                   quick_reply: Option[QuickReply])

case class Attachment(`type`: String, payload: Payload)

case class QuickReply(payload: String)

case class Payload(url: Option[String], coordinates: Option[Coordinates])

case class Coordinates(lat: Double, long: Double)

case class Postback(payload: JObject, referral: Option[Referral])

case class Referral(ref: JObject, source: String, `type`: String)
