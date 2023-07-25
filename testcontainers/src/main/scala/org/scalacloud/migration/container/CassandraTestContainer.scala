package org.scalacloud.migration.container

import com.dimafeng.testcontainers.CassandraContainer
import org.scalacloud.migration.Migration
import org.scalacloud.migration.config.{ CassandraHost, MigrationConfig }
import org.testcontainers.utility.DockerImageName

import zio.blocking.{ Blocking, effectBlocking }
import zio.logging.Logger
import zio.{ Has, ZIO, ZLayer, ZManaged }

object CassandraTestContainer {

  def live(
    keyspace: String,
    consistencyLevel: String,
    initScript: Option[String] = None,
    migrationsTablePrefix: String = "",
    dockerImageNameOverride: Option[DockerImageName] = None,
    runMigrations: Boolean = true
  ): ZLayer[Blocking with Has[Logger[String]], Nothing, Has[CassandraContainer]] =
    ZManaged.make {
      (for {
        container <- effectBlocking {
                       val container = new CassandraContainer(
                         dockerImageNameOverride = dockerImageNameOverride,
                         initScript = initScript
                       )
                       container.start()
                       container
                     }

        config = MigrationConfig(
                   List(CassandraHost(container.host, container.mappedPort(9042))),
                   "datacenter1",
                   keyspace,
                   container.username,
                   container.password,
                   consistencyLevel,
                   migrationsTablePrefix
                 )

        _ <- if (runMigrations)
               Migration.run(config)
             else
               ZIO.succeed(container)

      } yield container).orDie
    }(container => effectBlocking(container.stop()).orDie).toLayer
}
