package com.synerise.steam.worker

import com.typesafe.config.{Config, ConfigFactory}

object AppConfig {

  lazy val Config: Config = ConfigFactory.load()
  private val Worker = Config.getConfig("worker")

  object Kafka {
    private val kafkaConfig: Config = Worker.getConfig("kafka")
    val BootstrapServers: String = kafkaConfig.getString("bootstrap-servers")
    val Topic: String = kafkaConfig.getString("topic")
    val GroupId: String = kafkaConfig.getString("group-id")
    val ConsumerConfig: Config = kafkaConfig.getConfig("consumer")
  }
}
