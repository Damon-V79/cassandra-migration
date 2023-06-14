package org.scalacloud.migration

import com.typesafe.config.ConfigFactory
import org.scalacloud.migration.config.AppConfig

import zio.logging.Logger
import zio.logging.slf4j.Slf4jLogger
import zio.{ App => ZIOApp, ZLayer, _ }

object Application extends ZIOApp {

  override def run(args: List[String]) = {
    val action = for {
      logger <- ZIO.service[Logger[String]]
      config <- ZIO.service[AppConfig]
      _      <- logger.info("Run Cassandra migration")
      _      <- Migration.run(config.migrationConfig)
    } yield ()

    action
      .provideLayer(
        configLayer ++ loggingLayer
      )
      .exitCode

  }

  // internal

  private val configLayer = ZLayer.succeed(ConfigFactory.load()) >>> AppConfig.live

  private val loggingLayer = Slf4jLogger.makeWithAnnotationsAsMdc(List.empty)
}
