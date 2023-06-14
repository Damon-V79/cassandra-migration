package com.scalacloud.migration

import java.net.InetSocketAddress

import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.internal.core.metadata.DefaultEndPoint
import com.dimafeng.testcontainers.CassandraContainer
import org.scalacloud.migration.container.CassandraTestContainer

import zio.ZIO.service
import zio.blocking.Blocking
import zio.duration.durationInt
import zio.logging.slf4j.Slf4jLogger
import zio.test.{ DefaultRunnableSpec, TestAspect, ZSpec, assertTrue }

object MigrationSpec extends DefaultRunnableSpec {

  override def spec: ZSpec[_root_.zio.test.environment.TestEnvironment, Any] =
    suite("Cassandra migration")(
      testM("should successful run migrations") {
        for {
          container <- service[CassandraContainer]
          session    = CqlSession.builder
                         .addContactEndPoint(
                           new DefaultEndPoint(
                             InetSocketAddress
                               .createUnresolved(
                                 container.cassandraContainer.getHost(),
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
      }.provideCustomLayer(
        (Blocking.live ++ logger) >>> CassandraTestContainer.live("test_keyspace", "LOCAL_QUORUM")
      )
    ) @@ TestAspect.timeout(120.seconds)

  // internal

  private val logger = Slf4jLogger.makeWithAnnotationsAsMdc(List.empty)

}
