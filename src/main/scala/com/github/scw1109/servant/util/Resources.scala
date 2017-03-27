package com.github.scw1109.servant.util

import java.util.concurrent.{ExecutorService, ForkJoinPool}

import org.asynchttpclient._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * @author scw1109
  */
object Resources {

  private lazy val _executors = new ForkJoinPool()

  private lazy val _asyncHttpClient = new DefaultAsyncHttpClient(
    new DefaultAsyncHttpClientConfig.Builder()
      .setFollowRedirect(true)
      .build())

  def asyncHttpClient: AsyncHttpClient = _asyncHttpClient

  def executors: ExecutorService = _executors

  implicit class RichFutureResponse(listenableFuture: ListenableFuture[Response]) {

    val _future: Future[Response] = Helpers.toFuture(listenableFuture)

    def future: Future[Response] = _future

    def successWhen(f: Response => Boolean): Future[Response] = {
      _future.transform {
        case Success(response) =>
          if (f.apply(response)) {
            Success(response)
          } else {
            Failure(new RuntimeException(
              s"Response consider failed: ${response.getStatusCode} ${response.getStatusText}"))
          }
        case Failure(t) => Failure(t)
      }
    }
  }

  def executeAsyncHttpClient(buildRequest: AsyncHttpClient =>
    BoundRequestBuilder): RichFutureResponse = {
    buildRequest.apply(_asyncHttpClient).execute()
  }
}
