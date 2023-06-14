package org.scalacloud.migration.config

import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ValueReader

import zio.{ Has, URIO, ZIO, ZLayer }

case class AppConfig(
  migrationConfig: MigrationConfig
)

object AppConfig {

  val live: ZLayer[Has[Config], Nothing, Has[AppConfig]] =
    ZLayer.fromService((config: Config) => fromConfig(config))

  def fromConfig(config: Config): AppConfig = config.as[AppConfig]("migration.cassandra")

  def configs: URIO[Has[AppConfig], AppConfig] = ZIO.service[AppConfig]

  // internal

  implicit lazy val cassandraHostReader: ValueReader[CassandraHost] = ValueReader.relative { cfg =>
    CassandraHost(
      cassandraHost = cfg.as[String]("host"),
      connectionPort = cfg.as[Int]("port")
    )
  }

  implicit lazy val AppConfigReader: ValueReader[AppConfig] = ValueReader.relative { cfg =>
    AppConfig(
      migrationConfig = MigrationConfig(
        cassandraHosts = cfg.as[Seq[CassandraHost]]("hosts"),
        dataCenter = cfg.as[String]("data-center"),
        keyspace = cfg.as[String]("keyspace"),
        username = cfg.as[String]("username"),
        password = cfg.as[String]("password"),
        consistencyLevel = cfg.as[String]("consistency-level"),
        tablePrefix = cfg.as[Option[String]]("table-prefix").getOrElse("")
      )
    )
  }

}
