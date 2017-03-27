package com.github.scw1109.servant.connector.facebook

import com.github.scw1109.servant.connector.facebook.event.Messaging
import com.github.scw1109.servant.connector.{EventObject, Facebook}

/**
  * @author scw1109
  */
case class FacebookEventObject(source: Facebook,
                               rawEvent: Messaging) extends EventObject
