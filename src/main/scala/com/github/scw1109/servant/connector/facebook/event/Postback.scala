package com.github.scw1109.servant.connector.facebook.event

import org.json4s.JObject

/**
  * @author scw1109
  */
case class Postback(payload: JObject, referral: Option[Referral])
