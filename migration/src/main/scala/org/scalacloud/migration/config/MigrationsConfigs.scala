package org.scalacloud.migration.config

case class CassandraHost(cassandraHost: String, connectionPort: Int)

case class MigrationConfig(
  cassandraHosts: Seq[CassandraHost],
  dataCenter: String,
  keyspace: String,
  username: String,
  password: String,
  consistencyLevel: String,
  tablePrefix: String
)
