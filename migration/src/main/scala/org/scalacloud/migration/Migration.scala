package org.scalacloud.migration

import java.net.InetSocketAddress

import com.datastax.oss.driver.api.core.{ ConsistencyLevel, CqlSession }
import org.cognitor.cassandra.migration.{ Database, MigrationConfiguration, MigrationRepository, MigrationTask }
import org.scalacloud.migration.config.MigrationConfig

import zio.logging.Logger
import zio.{ Has, Task, ZIO, ZManaged }

object Migration {

  def run(config: MigrationConfig): ZIO[Has[Logger[String]], Throwable, Unit] = {
    val action = for {
      logger  <- ZIO.service[Logger[String]]
      _       <- logger.info("Booting...")
      _       <- logger.info("Connecting...")
      session <- connect(config)
      _       <- logger.info("Starting migration...")
      _       <- runMigration(session, config)
      _       <- logger.info("Migration completed")
    } yield ()

    action.tapError { _ =>
      for {
        logger <- ZIO.service[Logger[String]]
        _      <- logger.error("Migration failed with error")
      } yield ()
    }

  }

  // internal

  private def connect(config: MigrationConfig): Task[CqlSession] =
    Task {
      val builder = CqlSession.builder()

      config.cassandraHosts
        .foldLeft(builder) { (b, node) =>
          b.addContactPoint(new InetSocketAddress(node.cassandraHost, node.connectionPort))
        }
        .withLocalDatacenter(config.dataCenter)
        .withAuthCredentials(config.username, config.password)
        .build()
    }

  def runMigration(session: CqlSession, config: MigrationConfig): Task[Unit] = {
    val configuration =
      new MigrationConfiguration().withKeyspaceName(config.keyspace).withTablePrefix(config.tablePrefix)

    ZManaged.fromAutoCloseable(Task(new Database(session, configuration))).use { database =>
      database.setConsistencyLevel(getConsistencyLevel(config.consistencyLevel))
      val migration = new MigrationTask(database, new MigrationRepository())
      Task(migration.migrate())
    }
  }

  private def getConsistencyLevel(str: String): ConsistencyLevel =
    str.capitalize match {
      case "ANY"          => ConsistencyLevel.ANY
      case "ONE"          => ConsistencyLevel.ONE
      case "TWO"          => ConsistencyLevel.TWO
      case "THREE"        => ConsistencyLevel.THREE
      case "QUORUM"       => ConsistencyLevel.QUORUM
      case "ALL"          => ConsistencyLevel.ALL
      case "LOCAL_ONE"    => ConsistencyLevel.LOCAL_ONE
      case "LOCAL_QUORUM" => ConsistencyLevel.LOCAL_QUORUM
      case "EACH_QUORUM"  => ConsistencyLevel.EACH_QUORUM
      case "SERIAL"       => ConsistencyLevel.SERIAL
      case "LOCAL_SERIAL" => ConsistencyLevel.LOCAL_SERIAL
      case _              => throw new Error(s"Unknown type of consistency level: $str")
    }

}
