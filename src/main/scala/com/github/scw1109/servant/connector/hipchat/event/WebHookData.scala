package com.github.scw1109.servant.connector.hipchat.event

/**
  * @author scw1109
  */
case class WebHookData(event: String,
                       item: Item,
                       oauth_client_id: Option[String],
                       webhook_id: Long)






