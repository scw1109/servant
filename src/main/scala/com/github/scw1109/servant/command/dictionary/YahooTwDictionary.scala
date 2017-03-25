package com.github.scw1109.servant.command.dictionary

import java.nio.charset.StandardCharsets

import com.github.scw1109.servant.command.{CommandRequest, CommandResponse}
import com.github.scw1109.servant.core.session.TextMessageRef
import com.github.scw1109.servant.util.{Helper, Resources}
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
  * @author scw1109
  */
class YahooTwDictionary extends Dictionary {

  private val yahooTwDictBaseUrl = "https://tw.dictionary.search.yahoo.com"

  override def searchDictionary(word: String,
                                request: CommandRequest): Future[CommandResponse] = {

    val yahooTwDictUrl = s"$yahooTwDictBaseUrl/search?p=${Helper.urlEncode(word)}"

    Resources.executeAsyncHttpClient {
      _.prepareGet(yahooTwDictUrl)
    } successWhen {
      _.getStatusCode == 200
    } transform {
      case Success(response) =>
        val body = response.getResponseBody(StandardCharsets.UTF_8)
        handleResponseBody(request, body)
      case Failure(t) => Failure(t)
    }
  }

  private def handleResponseBody(request: CommandRequest,
                                 body: String): Try[CommandResponse] = {

    val doc = Jsoup.parse(body)
    val explains = doc.select("div.algo.explain.DictionaryResults")
      .first()
      .select("ul.compArticleList > li > h4 > span")
      .toArray(Array[Element]())
      .take(3)
    val meaning = explains.map(_.text()).mkString("\n")

    Success(TextMessageRef(s"= Yahoo TW dictionary =\n$meaning", request))

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
