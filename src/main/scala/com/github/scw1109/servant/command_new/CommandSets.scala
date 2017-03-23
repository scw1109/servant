package com.github.scw1109.servant.command_new

import akka.actor.Props
import com.github.scw1109.servant.command_new.dictionary.{DictionaryDotCom, UrbanDictionary, YahooTwDictionary}
import com.github.scw1109.servant.command_new.earthquake.Earthquake
import com.github.scw1109.servant.command_new.echo.Echo

/**
  * @author scw1109
  */
object CommandSets {

  def default = Seq(
    Props(classOf[Echo]),
    Props(classOf[Earthquake]),
    Props(classOf[UrbanDictionary]),
    Props(classOf[YahooTwDictionary]),
    Props(classOf[DictionaryDotCom])
  )
}
