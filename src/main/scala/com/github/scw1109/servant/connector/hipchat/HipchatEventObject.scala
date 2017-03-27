package com.github.scw1109.servant.connector.hipchat

import com.github.scw1109.servant.connector.hipchat.event.WebHookData
import com.github.scw1109.servant.connector.{EventObject, Hipchat}

/**
  * @author scw1109
  */
case class HipchatEventObject(source: Hipchat,
                              rawEvent: WebHookData) extends EventObject
