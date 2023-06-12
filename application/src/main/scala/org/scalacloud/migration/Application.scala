package org.scalacloud.migration

import com.typesafe.config.ConfigFactory
import org.scalacloud.migration.config.AppConfig

import zio.logging.backend.SLF4J
import zio.{ ZIOAppDefault, ZLayer, _ }

object Application extends ZIOAppDefault {

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  override def run: ZIO[Any, Any, Any] = {
    val action = for {
      config <- ZIO.service[AppConfig]
      _      <- ZIO.logInfo("Run Cassandra migration")
      _      <- Migration.run(config.migrationConfig)
    } yield ()

    action
      .provide(
        AppConfig.live,
        ZLayer.succeed(ConfigFactory.load())
      )
      .exitCode
  }

}
