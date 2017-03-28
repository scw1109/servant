package com.github.scw1109.servant.command

import akka.actor.Props
import com.github.scw1109.servant.command.dictionary.{DictionaryDotCom, UrbanDictionary, YahooTwDictionary}
import com.github.scw1109.servant.command.earthquake.Earthquake
import com.github.scw1109.servant.command.echo.Echo
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.{Logger, LoggerFactory}

/**
  * @author scw1109
  */
object CommandSets {

  private val logger: Logger = LoggerFactory.getLogger(getClass)

  private val commandActorClasses: Seq[Class[_ <: CommandActor]] = Seq(
    classOf[Echo],
    classOf[Earthquake],
    classOf[UrbanDictionary],
    classOf[YahooTwDictionary],
    classOf[DictionaryDotCom]
  )

  private val commandSetsDefinition: Map[String,
    Map[Class[_ <: CommandActor], Props] => Seq[Props]] =
    Map(
      fullSetKey -> {
        _.values.toSeq
      },
      defaultSetKey -> {
        _.values.toSeq
      },
      "echo-only" -> {
        _.filterKeys(classOf[Echo].eq(_))
          .values.toSeq
      }
    )

  private var _commandProps: Map[Class[_ <: CommandActor], Props] = _

  private var _commandSets: Map[String, Seq[Props]] = _

  def fullSetKey = "all"

  def defaultSetKey = "default"

  def build(configs: Map[String, Config]): Unit = {
    _commandProps = commandActorClasses.map(clazz => {
      logger.trace(s"Registering command: ${clazz.getSimpleName}")
      configs.get(clazz.getSimpleName.toLowerCase) match {
        case Some(config) => clazz -> Props(clazz, config)
        case None => clazz -> Props(clazz, ConfigFactory.empty())
      }
    }).toMap

    _commandSets = commandSetsDefinition
      .map(d => d._1 -> d._2.apply(_commandProps))
  }

  def get(key: String): Seq[Props] = {
    _commandSets.getOrElse(key, Seq())
  }
}
