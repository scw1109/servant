package com.github.scw1109.servant.connector.line

import com.github.scw1109.servant.message.{EventSource, EventSourceType, LineType}
import org.json4s.{DefaultFormats, Formats, TypeHints}

/**
  * @author scw1109
  */
sealed trait LineEvent extends EventSource {

  override def getType: EventSourceType = LineType()

  def `type`: String

  def timestamp: Long

  def source: Source
}

sealed trait ReplyToken {

  def replyToken: String
}

case class MessageEvent(`type`: String = "message", timestamp: Long,
                        source: Source, replyToken: String,
                        message: Message) extends LineEvent with ReplyToken

case class FollowEvent(`type`: String = "follow", timestamp: Long,
                       source: Source, replyToken: String
                      ) extends LineEvent with ReplyToken

case class UnfollowEvent(`type`: String = "unfollow", timestamp: Long,
                       source: Source) extends LineEvent

case class JoinEvent(`type`: String = "join", timestamp: Long,
                       source: Source, replyToken: String
                      ) extends LineEvent with ReplyToken

case class LeaveEvent(`type`: String = "leave", timestamp: Long,
                         source: Source) extends LineEvent

case class PostbackEvent(`type`: String = "postback", timestamp: Long,
                         source: Source, replyToken: String,
                         `postback.data`: String) extends LineEvent with ReplyToken

trait Source {

  def `type`: String
}

case class User(`type`: String = "user", userId: String) extends Source

case class Group(`type`: String = "group", groupId: String) extends Source

case class Room(`type`: String = "room", roomId: String) extends Source

trait Message {

  def id: String

  def `type`: String
}

case class TextMessage(`type`: String = "text", id: String,
                       text: String) extends Message

case class ImageMessage(`type`: String = "image", id: String) extends Message

case class VideoMessage(`type`: String = "video", id: String) extends Message

case class AudioMessage(`type`: String = "audio", id: String) extends Message

case class LocationMessage(`type`: String = "location", id: String,
                           title: String, address: String,
                           latitude: Double, longitude: Double) extends Message

case class StickerMessage(`type`: String = "sticker", id: String,
                          packageId: String, stickerId: String) extends Message

object LineFormats {

  val format: Formats = DefaultFormats.withHints(LineTypeHints)
    .withTypeHintFieldName("type")
}

object LineTypeHints extends TypeHints {

  val classToHint: Map[Class[_], String] = Map(
    // Event
    classOf[MessageEvent] -> "message",
    classOf[FollowEvent] -> "follow",
    classOf[UnfollowEvent] -> "unfollow",
    classOf[JoinEvent] -> "join",
    classOf[LeaveEvent] -> "leave",
    classOf[PostbackEvent] -> "postback",

    // Source
    classOf[User] -> "user",
    classOf[Group] -> "group",
    classOf[Room] -> "room",

    // Message
    classOf[TextMessage] -> "text",
    classOf[ImageMessage] -> "image",
    classOf[VideoMessage] -> "video",
    classOf[AudioMessage] -> "audio",
    classOf[StickerMessage] -> "sticker",
    classOf[LocationMessage] -> "location"
  )
  val hintToClass: Map[String, Class[_]] = classToHint.map(_.swap)

  override val hints: List[Class[_]] = classToHint.keySet.toList

  override def classFor(hint: String): Option[Class[_]] = hintToClass.get(hint)

  override def hintFor(clazz: Class[_]): String = classToHint(clazz)
}