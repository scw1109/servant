package com.github.scw1109.servant.connector.facebook.event

import org.json4s.JObject

/**
  * @author scw1109
  */
case class Referral(ref: JObject, source: String, `type`: String)
