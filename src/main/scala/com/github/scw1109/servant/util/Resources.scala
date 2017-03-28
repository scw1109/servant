package com.github.scw1109.servant.util

import java.nio.charset.StandardCharsets
import java.util.concurrent.{ExecutorService, ForkJoinPool}

import org.asynchttpclient._

import scala.concurrent.ExecutionContext
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

  def executorService: ExecutorService = _executors

  def executionContext: ExecutionContext = ExecutionContext.fromExecutorService(_executors)

  implicit class RichFutureResponse(listenableFuture: ListenableFuture[Response])
                                   (implicit executorService: ExecutorService) {

    val _future: Future[Response] = Helpers.toFuture(listenableFuture)

    def future: Future[Response] = _future

    def successWhen(f: Response => Boolean)
                   (implicit executor: ExecutionContext): Future[Response] = {
      _future.transform {
        case Success(response) =>
          if (f.apply(response)) {
            Success(response)
          } else {
            Failure(new RuntimeException(
              s"Response consider failed: ${response.getStatusCode} ${response.getStatusText}\n" +
                s"${response.getResponseBody(StandardCharsets.UTF_8)}"))
          }
        case Failure(t) => Failure(t)
      }
    }
  }

  def executeAsyncHttpClient(f: AsyncHttpClient => BoundRequestBuilder)
                            (implicit executorService: ExecutorService): RichFutureResponse = {
    f.apply(_asyncHttpClient).execute()
  }
}
