package org.scalacloud.migration.container

import com.dimafeng.testcontainers.CassandraContainer
import org.scalacloud.migration.Migration
import org.scalacloud.migration.config.{ CassandraHost, MigrationConfig }
import org.testcontainers.utility.DockerImageName

import zio.{ ULayer, ZIO, ZLayer }

object CassandraTestContainer {

  def live(
    keyspace: String,
    consistencyLevel: String,
    initScript: Option[String] = None,
    migrationsTablePrefix: String = "",
    dockerImageNameOverride: Option[DockerImageName] = None,
    runMigrations: Boolean = true
  ): ULayer[CassandraContainer] =
    ZLayer.scoped {
      ZIO.acquireRelease {
        (for {
          container <- ZIO.attemptBlocking {
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
      }(container => ZIO.attemptBlocking(container.stop()).orDie)
    }
}
