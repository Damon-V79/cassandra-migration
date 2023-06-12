package org.scalacloud.migration

import java.net.InetSocketAddress

import com.datastax.oss.driver.api.core.{ ConsistencyLevel, CqlSession }
import org.cognitor.cassandra.migration.{ Database, MigrationConfiguration, MigrationRepository, MigrationTask }
import org.scalacloud.migration.config.MigrationConfig

import zio.{ Cause, Task, ZIO }

object Migration {

  def run(config: MigrationConfig): ZIO[Any, Throwable, Unit] = {
    val action = for {
      _       <- ZIO.logInfo("Booting...")
      _       <- ZIO.logInfo("Connecting...")
      session <- connect(config)
      _       <- ZIO.logInfo("Starting migration...")
      _       <- runMigration(session, config)
      _       <- ZIO.logInfo("Migration completed")
    } yield ()

    action.tapError { t =>
      for {
        _ <- ZIO.logErrorCause("Migration failed with error", Cause.fail(t))
      } yield ()
    }

  }

  // internal

  private def connect(config: MigrationConfig): Task[CqlSession] =
    ZIO.attempt {
      val builder = CqlSession.builder()

      config.cassandraHosts
        .foldLeft(builder) { (b, node) =>
          b.addContactPoint(new InetSocketAddress(node.cassandraHost, node.connectionPort))
        }
        .withLocalDatacenter(config.dataCenter)
        .withAuthCredentials(config.username, config.password)
        .build()
    }

  private def runMigration(session: CqlSession, config: MigrationConfig): Task[Unit] =
    ZIO.scoped {
      val configuration =
        new MigrationConfiguration().withKeyspaceName(config.keyspace).withTablePrefix(config.tablePrefix)
      ZIO.fromAutoCloseable(ZIO.attempt(new Database(session, configuration))).flatMap { database =>
        database.setConsistencyLevel(getConsistencyLevel(config.consistencyLevel))
        val migration = new MigrationTask(database, new MigrationRepository())
        ZIO.attempt(migration.migrate())
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
