package com.github.scw1109.servant.command.dictionary

import com.github.scw1109.servant.Servant
import com.github.scw1109.servant.command.Command
import com.github.scw1109.servant.message.{IncomingMessage, RichOutgoingMessage, SlackType, TextOutgoingMessage}
import com.github.scw1109.servant.util.Helper
import com.typesafe.config.Config
import org.asynchttpclient._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

/**
  * @author scw1109
  */
object Dictionary extends Command {

  private val asyncHttpClient: AsyncHttpClient =
    new DefaultAsyncHttpClient(
      new DefaultAsyncHttpClientConfig.Builder()
        .setFollowRedirect(true)
        .build())

  override def init(config: Config): Unit = {}

  override def accept(incomingMessage: IncomingMessage): Boolean = {
    val text = incomingMessage.text.trim
    text.startsWith("dict")
  }

  override def execute(incomingMessage: IncomingMessage): Unit = {
    val word = incomingMessage.text.trim.substring("dict".length).trim

    yahooTwDictionary(word, incomingMessage)
    urbanDictionary(word, incomingMessage)
    dictionaryDotCom(word, incomingMessage)
  }

  private def urbanDictionary(word: String, incomingMessage: IncomingMessage) = {
    val urbanBaseUrl = "http://www.urbandictionary.com"
    val urbanUrl = s"$urbanBaseUrl/define.php?term=${Helper.urlEncode(word)}"

    asyncHttpClient
      .prepareGet(urbanUrl)
      .execute(new AsyncCompletionHandler[Unit] {
        override def onCompleted(response: Response): Unit = {
          if (response.getStatusCode == 200) {
            val body = response.getResponseBody
            val doc = Jsoup.parse(body)
            val meaning = doc.select("div.def-panel div.meaning").first().text()

            incomingMessage.source.getType match {
              case SlackType() =>
                val attachments = List(
                  ("color" -> "#ff8800") ~
                    ("title" -> word) ~
                    ("title_link" -> urbanUrl) ~
                    ("footer" -> "Urban dictionary") ~
                    ("footer_icon" -> s"$urbanBaseUrl/favicon.ico")
                )

                Servant.sendResponse(
                  RichOutgoingMessage(meaning, compact(render(attachments))),
                  incomingMessage)

              case _ =>
                Servant.sendResponse(
                  TextOutgoingMessage(meaning),
                  incomingMessage)
            }
          }
        }
      })
  }

  def yahooTwDictionary(word: String, incomingMessage: IncomingMessage): Unit = {
    val yahooDictBaseUrl = "https://tw.dictionary.search.yahoo.com"
    val yahooDictUrl = s"$yahooDictBaseUrl/search?p=${Helper.urlEncode(word)}"

    asyncHttpClient
      .prepareGet(yahooDictUrl)
      .execute(new AsyncCompletionHandler[Unit] {
        override def onCompleted(response: Response): Unit = {
          if (response.getStatusCode == 200) {
            val body = response.getResponseBody
            val doc = Jsoup.parse(body)
            val explains = doc.select("div.algo.explain.DictionaryResults")
              .first()
              .select("ul.compArticleList > li > h4 > span")
              .toArray(Array[Element]())
              .take(3)
            val meaning = explains.map(_.text()).mkString("\n")

            incomingMessage.source.getType match {
              case SlackType() =>
                val attachments = List(
                  ("color" -> "#7b0099") ~
                    ("title" -> word) ~
                    ("title_link" -> yahooDictUrl) ~
                    ("footer" -> "Yahoo TW dictionary") ~
                    ("footer_icon" -> s"$yahooDictBaseUrl/favicon.ico")
                )

                Servant.sendResponse(
                  RichOutgoingMessage(meaning, compact(render(attachments))),
                  incomingMessage)

              case _ =>
                Servant.sendResponse(
                  TextOutgoingMessage(meaning),
                  incomingMessage)
            }
          }
        }
      })
  }

  def dictionaryDotCom(word: String, incomingMessage: IncomingMessage): Unit = {
    val dictBaseUrl = "http://www.dictionary.com"
    val dictUrl = s"$dictBaseUrl/browse/${Helper.urlEncode(word)}"

    asyncHttpClient
      .prepareGet(dictUrl)
      .execute(new AsyncCompletionHandler[Unit] {
        override def onCompleted(response: Response): Unit = {
          if (response.getStatusCode == 200) {
            val body = response.getResponseBody
            val doc = Jsoup.parse(body)
            val explains = doc.select("div.def-content")
              .toArray(Array[Element]())
              .take(3)
            val meaning = explains.zipWithIndex.map {
              case (e, i) =>
                s"${i+1}. ${e.text()}"
            }.mkString("\n")

            incomingMessage.source.getType match {
              case SlackType() =>
                val attachments = List(
                  ("color" -> "#307dbc") ~
                    ("title" -> word) ~
                    ("title_link" -> dictUrl) ~
                    ("footer" -> "Dictionary.com") ~
                    ("footer_icon" -> s"$dictBaseUrl/favicon.ico")
                )

                Servant.sendResponse(
                  RichOutgoingMessage(meaning, compact(render(attachments))),
                  incomingMessage)

              case _ =>
                Servant.sendResponse(
                  TextOutgoingMessage(meaning),
                  incomingMessage)
            }
          }
        }
      })
  }
}
