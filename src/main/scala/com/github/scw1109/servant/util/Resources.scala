package com.github.scw1109.servant.util

import java.util.concurrent.{ExecutorService, ForkJoinPool}

import org.asynchttpclient.{AsyncHttpClient, DefaultAsyncHttpClient}

/**
  * @author scw1109
  */
object Resources {

  private lazy val _executors = new ForkJoinPool()
  private lazy val _asyncHttpClient = new DefaultAsyncHttpClient()

  def asyncHttpClient: AsyncHttpClient = _asyncHttpClient

  def executors: ExecutorService = _executors
}
