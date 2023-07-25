package org.scalacloud.migration

import java.net.InetSocketAddress

import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.internal.core.metadata.DefaultEndPoint
import com.dimafeng.testcontainers.CassandraContainer
import org.scalacloud.migration.container.CassandraTestContainer

import zio.ZIO.service
import zio._
import zio.logging.backend.SLF4J
import zio.test.{ Spec, TestAspect, ZIOSpecDefault, assertTrue }

object MigrationSpec extends ZIOSpecDefault {

  override def spec: Spec[Any, Any] =
    suite("Cassandra migration")(
      test("should successful run migrations") {
        for {
          container <- service[CassandraContainer]
          session    = CqlSession.builder
                         .addContactEndPoint(
                           new DefaultEndPoint(
                             InetSocketAddress
                               .createUnresolved(
                                 container.cassandraContainer.getHost,
                                 container.cassandraContainer.getFirstMappedPort.intValue()
                               )
                           )
                         )
                         .withLocalDatacenter("datacenter1")
                         .build()

          // Read from `test_keyspace`
          rsOne      = session.execute(
                         "SELECT value FROM test_keyspace.test_string_by_id WHERE id = 0b54a376-9aba-43bd-be21-4c20aa0445d4"
                       )
          rowOne     = rsOne.one
          resultOne  = rowOne.getString("value")

          // Read from `other_keyspace`
          rsTwo     = session.execute("SELECT value FROM other_keyspace.test_integer_by_id WHERE id = 2")
          rowTwo    = rsTwo.one
          resultTwo = rowTwo.getInt("value")
        } yield assertTrue(resultOne == "text1") && assertTrue(resultTwo == 42)
      }.provide(
        logger >>> CassandraTestContainer.live(
          keyspace = "test_keyspace",
          consistencyLevel = "LOCAL_QUORUM",
          initScript = Some("initScript.cql")
        )
      )
    ) @@ TestAspect.timeout(120.seconds)

  // internal

  private val logger = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

}
