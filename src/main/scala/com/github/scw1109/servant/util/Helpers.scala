package com.github.scw1109.servant.util

import java.net.URLEncoder
import java.nio.file.{Files, Paths}
import java.util.Properties
import java.util.concurrent.ExecutorService

import org.asynchttpclient.ListenableFuture

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}

/**
  * @author scw1109
  */
object Helpers {

  def loadDevEnv(): Boolean = {
    val path = Paths.get("dev.secret")
    if (Files.exists(path)) {
      val props = new Properties()
      props.load(Files.newInputStream(path))
      props.forEach((k, v) => {
        System.setProperty(k.toString, v.toString)
      })
      true
    } else {
      false
    }
  }

  def urlEncode(s: String): String = {
    URLEncoder.encode(s, "utf-8")
  }

  implicit class RichListenableFuture[T](listenableFuture: ListenableFuture[T])
                                        (implicit executorService: ExecutorService) {
    def asScala(): Future[T] = {
      val promise = Promise[T]()
      listenableFuture.addListener(() => {
        Try {
          listenableFuture.get()
        } match {
          case Success(s) => promise.success(s)
          case Failure(t) => promise.failure(t)
        }
      }, executorService)
      promise.future
    }
  }

  def toFuture[T](listenableFuture: ListenableFuture[T])
                 (implicit executorService: ExecutorService): Future[T] = {
    listenableFuture.asScala()
  }
}
