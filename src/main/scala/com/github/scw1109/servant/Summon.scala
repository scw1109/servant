package com.github.scw1109.servant

import com.github.scw1109.servant.util.Helper

/**
  * @author scw1109
  */
object Summon extends App {

  Helper.loadDevEnv()
  Servant.start()
}
