package com.github.scw1109.servant.connector_new.facebook.model

import org.json4s.JObject

/**
  * @author scw1109
  */
case class Postback(payload: JObject, referral: Option[Referral])
