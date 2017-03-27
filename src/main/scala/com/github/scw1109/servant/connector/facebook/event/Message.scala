package com.github.scw1109.servant.connector.facebook.event

/**
  * @author scw1109
  */
case class Message(mid: String, text: String, attachments: List[Attachment],
                   quick_reply: Option[QuickReply])
