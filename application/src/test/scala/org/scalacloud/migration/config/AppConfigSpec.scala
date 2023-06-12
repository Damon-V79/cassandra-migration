package org.scalacloud.migration.config

import com.typesafe.config.ConfigFactory

import zio.test.{ Spec, TestEnvironment, ZIOSpecDefault, assertTrue }
import zio.{ Scope, ZIO }

object AppConfigSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("AppConfig")(
      test("should correct read config from Typesafe config") {
        ZIO.attempt {
          val config = AppConfig.fromConfig(
            ConfigFactory.parseString(
              """{
                  migration: {
                    cassandra: {
                      hosts = [
                        { host = "host1", port = 42 }
                        { host = "host2", port = 43 }
                      ]
                      data-center = datacenter1
                      keyspace = keyspace
                      username = username
                      password = password
                      consistency-level = LOCAL_QUORUM
                      table-prefix = prefix
                    }
                  }
               }
            """
            )
          )

          val expected = AppConfig(
            migrationConfig = MigrationConfig(
              cassandraHosts = List(
                CassandraHost("host1", 42),
                CassandraHost("host2", 43)
              ),
              dataCenter = "datacenter1",
              keyspace = "keyspace",
              username = "username",
              password = "password",
              consistencyLevel = "LOCAL_QUORUM",
              tablePrefix = "prefix"
            )
          )

          assertTrue(config == expected)
        }
      },
      test("should correct read config from Typesafe config without optional parameters") {
        ZIO.attempt {
          val config = AppConfig.fromConfig(
            ConfigFactory.parseString(
              """{
                  migration: {
                    cassandra: {
                      hosts = [
                        { host = "host1", port = 42 }
                        { host = "host2", port = 43 }
                      ]
                      data-center = datacenter1
                      keyspace = keyspace
                      username = username
                      password = password
                      consistency-level = LOCAL_QUORUM
                    }
                  }
               }
            """
            )
          )

          val expected = AppConfig(
            migrationConfig = MigrationConfig(
              cassandraHosts = List(
                CassandraHost("host1", 42),
                CassandraHost("host2", 43)
              ),
              dataCenter = "datacenter1",
              keyspace = "keyspace",
              username = "username",
              password = "password",
              consistencyLevel = "LOCAL_QUORUM",
              tablePrefix = ""
            )
          )

          assertTrue(config == expected)
        }
      }
    )

}
