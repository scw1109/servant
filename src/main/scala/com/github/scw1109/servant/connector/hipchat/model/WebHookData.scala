package com.github.scw1109.servant.connector.hipchat.model

/**
  * @author scw1109
  */
case class WebHookData(event: String,
                       item: Item,
                       oauth_client_id: Option[String],
                       webhook_id: Long)

object WebHookDataRef {

  def unapply(webHookData: WebHookData): Option[WebHookData] = {
    Some(webHookData)
  }
}






