import sbt._

object Dependencies {

  object V {
    val cassandraMigrationVersion = "2.6.0_v4"
    val configVersion             = "1.4.2"
    val datastaxJavaDriver        = "4.16.0"
    val diffxCoreVersion          = "0.7.0"
    val ficusVersion              = "1.5.2"
    val jacksonDatabindVersion    = "2.15.0"
    val logbackClassicVersion     = "1.4.8"
    val logbackJsonVersion        = "0.1.5"
    val testcontainersScala       = "0.40.16"
    val zioLoggingVersion         = "0.5.14"
    val zioVersion                = "1.0.18"
  }

  import V._

  val cassandraDependencies = Seq(
    "org.cognitor.cassandra" % "cassandra-migration" % cassandraMigrationVersion,
    "com.datastax.oss"       % "java-driver-core"    % datastaxJavaDriver
  )

  val zioDependencies = Seq(
    "dev.zio" %% "zio"         % zioVersion,
    "dev.zio" %% "zio-logging" % zioLoggingVersion
  )

  val logs = Seq(
    "ch.qos.logback"             % "logback-classic"      % logbackClassicVersion,
    "ch.qos.logback.contrib"     % "logback-jackson"      % logbackJsonVersion,
    "ch.qos.logback.contrib"     % "logback-json-classic" % logbackJsonVersion,
    "com.fasterxml.jackson.core" % "jackson-databind"     % jacksonDatabindVersion,
    "dev.zio"                   %% "zio-logging-slf4j"    % zioLoggingVersion
  )

  val configs = Seq(
    "com.typesafe" % "config" % configVersion,
    "com.iheart"  %% "ficus"  % ficusVersion
  )

  val testcontainers = Seq(
    "com.dimafeng" %% "testcontainers-scala-core"      % testcontainersScala,
    "com.dimafeng" %% "testcontainers-scala-cassandra" % testcontainersScala
  )

  val tests = Seq(
    "dev.zio" %% "zio-test"      % zioVersion,
    "dev.zio" %% "zio-test-sbt"  % zioVersion
  )

}
