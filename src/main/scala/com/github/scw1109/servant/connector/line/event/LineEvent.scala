package com.github.scw1109.servant.connector.line.event

/**
  * @author scw1109
  */
sealed trait LineEvent {

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
