package com.github.scw1109.servant.connector.facebook.model

import org.json4s.JObject

/**
  * @author scw1109
  */
case class Referral(ref: JObject, source: String, `type`: String)
