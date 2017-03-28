package com.github.scw1109.servant.command.dictionary

import java.nio.charset.StandardCharsets

import com.github.scw1109.servant.core.session.{Reply, SessionEvent}
import com.github.scw1109.servant.util.{Helpers, Resources}
import com.typesafe.config.Config
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * @author scw1109
  */
class YahooTwDictionary(config: Config) extends Dictionary(config) {

  private val yahooTwDictBaseUrl = "https://tw.dictionary.search.yahoo.com"

  override def search(word: String, sessionEvent: SessionEvent): Future[Option[Reply]] = {

    val yahooTwDictUrl = s"$yahooTwDictBaseUrl/search?p=${Helpers.urlEncode(word)}"

    Resources.executeAsyncHttpClient {
      _.prepareGet(yahooTwDictUrl)
    } successWhen {
      _.getStatusCode == 200
    } transform {
      case Success(response) =>
        val body = response.getResponseBody(StandardCharsets.UTF_8)
        Success(handleResult(sessionEvent, body))
      case Failure(t) => Failure(t)
    }
  }

  private def handleResult(sessionEvent: SessionEvent, body: String): Option[Reply] = {
    val doc = Jsoup.parse(body)
    val explains = doc.select("div.algo.explain.DictionaryResults")
      .first()
      .select("ul.compArticleList > li > h4 > span")
      .toArray(Array[Element]())
      .take(3)
    val meaning = explains.map(_.text()).mkString("\n")

    Option(Reply(s"= Yahoo TW dictionary =\n$meaning", sessionEvent.event))

    //            incomingMessage.source.getType match {
    //              case SlackType() =>
    //                val attachments = buildAttachments(word, yahooTwDictUrl,
    //                  "Yahoo TW dictionary", s"$yahooTwDictBaseUrl/favicon.ico",
    //                  "#7b0099")
    //
    //                Servant.sendResponse(
    //                  RichOutgoingMessage(meaning, attachments),
    //                  incomingMessage)
  }
}
