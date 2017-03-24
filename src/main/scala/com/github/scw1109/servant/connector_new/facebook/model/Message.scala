package com.github.scw1109.servant.connector_new.facebook.model

/**
  * @author scw1109
  */
case class Message(mid: String, text: String, attachments: List[Attachment],
                   quick_reply: Option[QuickReply])
