package com.synerise.steam.master

import com.typesafe.config.{Config, ConfigFactory}

object AppConfig {

  lazy val Config: Config = ConfigFactory.load()
  private val Master = Config.getConfig("master")

  object Websocket {
    private val websocket = Master.getConfig("websocket")
    val ThrottleElements: Int = websocket.getInt("throttle-elements")
    val ThrottleSeconds: Int = websocket.getInt("throttle-seconds")
  }

}
