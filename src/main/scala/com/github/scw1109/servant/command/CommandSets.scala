package com.github.scw1109.servant.command

import akka.actor.Props
import com.github.scw1109.servant.command.dictionary.{DictionaryDotCom, UrbanDictionary, YahooTwDictionary}
import com.github.scw1109.servant.command.earthquake.Earthquake
import com.github.scw1109.servant.command.echo.Echo

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
