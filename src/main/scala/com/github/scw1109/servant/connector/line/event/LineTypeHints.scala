package com.github.scw1109.servant.connector.line.event

import org.json4s.TypeHints

/**
  * @author scw1109
  */
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
