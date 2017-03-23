package com.github.scw1109.servant.util

import java.net.URLEncoder
import java.nio.file.{Files, Paths}
import java.util.Properties

import org.asynchttpclient.ListenableFuture

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}

/**
  * @author scw1109
  */
object Helper {

  def loadDevEnv(): Boolean = {
    val path = Paths.get("dev.secret")
    if (Files.exists(path)) {
      val props = new Properties()
      props.load(Files.newInputStream(path))
      props.forEach((k, v) => {
        System.setProperty(k.toString, v.toString)
      })
      System.setProperty("SERVANT_MODE", "development")
      true
    } else {
      false
    }
  }

  def urlEncode(s: String): String = {
    URLEncoder.encode(s, "utf-8")
  }

  implicit class RichListenableFuture[T](listenableFuture: ListenableFuture[T]) {
    def asScala(): Future[T] = {
      val promise = Promise[T]()
      listenableFuture.addListener(() => {
        Try {
          listenableFuture.get()
        } match {
          case Success(s) => promise.success(s)
          case Failure(t) => promise.failure(t)
        }
      }, Resources.executors)
      promise.future
    }
  }

  def toFuture[T](listenableFuture: ListenableFuture[T]): Future[T] = {
    listenableFuture.asScala()
  }

  def toFuture[T](v: T): Future[T] = {
    val promise = Promise[T]()
    promise.success(v)
    promise.future
  }
}
