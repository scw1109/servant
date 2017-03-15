package com.github.scw1109.servant.connector.hipchat

import com.github.scw1109.servant.message.{EventSource, EventSourceType, HipchatType}

/**
  * @author scw1109
  */
case class HipchatEvent(event: String,
                        item: Item,
                        oauth_client_id: Option[String],
                        webhook_id: Long) extends EventSource {

  override def getType: EventSourceType = HipchatType()
}

case class Item(message: Message, room: Room)

case class Message(date: String, id: String, `type`: String,
                   message: String)

case class Room(id: Long, is_archived: Boolean, links: Links,
                name: String, privacy: String, version: String)

case class Links(members: Option[String], participants: String,
                 self: String, webhooks: String)