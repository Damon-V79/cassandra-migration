import ReleaseTransformations._

ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"

ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"

releaseCrossBuild := true
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,             // check that there are no SNAPSHOT dependencies
  inquireVersions,                       // ask user to enter the current and next verion
  runClean,                              // clean 
  runTest,                               // run tests
  setReleaseVersion,                     // set release version in version.sbt 
  commitReleaseVersion,                  // commit the release version 
  tagRelease,                            // create git tag
  releaseStepCommandAndRemaining("+publishSigned"), // run +publishSigned command to sonatype stage release
  setNextVersion,                        // set next version in version.sbt
  commitNextVersion,                     // commint next version
  //releaseStepCommand("sonatypeRelease"), // run sonatypeRelease and publish to maven central
  pushChanges                            // push changes to git
)

lazy val scala212 = "2.12.18"
lazy val scala213 = "2.13.10"
lazy val scala3   = "3.3.0"
lazy val supportedScalaVersions = List(scala212, scala213, scala3)

val commonSettings = Seq(
  scalaVersion := scala213,
  crossScalaVersions := supportedScalaVersions,
  organization := "org.scalacloud",
  homepage := Some(url("https://scalacloud.org/cassandra-migration")),
  scmInfo := Some(ScmInfo(url("https://github.com/Damon-V79/cassandra-migration"), "https://github.com/Damon-V79/cassandra-migration.git")),
  developers := List(Developer("DamonV79", "Dmitry Voevodin", "damon@damonblog.com", url("https://damonblog.com"))),
  licenses := Seq("MIT" -> url("https://opensource.org/license/mit/")),
  publishMavenStyle := true,
  testFrameworks ++= Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 12)) => scalacOptions212
      case Some((2, 13)) => scalacOptions213
      case _             => Nil
    }
  },
  Test / parallelExecution := false,
  publishTo := {
    val nexus = "https://s01.oss.sonatype.org/"
    if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
    else Some("releases" at nexus + "service/local/staging/deploy/maven2")
  }
)

lazy val itSettings = Defaults.itSettings ++ scalafixConfigSettings(IntegrationTest)

lazy val migration = (project in file("migration"))
  .settings(commonSettings: _*)
  .settings(
    name := "cassandra-migration",
    libraryDependencies ++=
      Dependencies.cassandraDependencies ++
        Dependencies.zioDependencies 
  )

lazy val application = (project in file("application"))
  .settings(commonSettings: _*)
  .enablePlugins(JavaServerAppPackaging, UniversalPlugin)
  .settings(
    name := "cassandra-migration-application",
    publish / skip := true,
    libraryDependencies ++=
      Dependencies.logs ++
	Dependencies.configs ++
        Dependencies.tests.map(_ % Test)
  )
  .dependsOn(migration)

lazy val testcontainers = (project in file("testcontainers"))
  .configs(IntegrationTest.extend(Test))
  .settings(itSettings)
  .settings(commonSettings: _*)
  .settings(
    name := "cassandra-migration-testcontainers",
    libraryDependencies ++=
      Dependencies.testcontainers ++
        Dependencies.tests.map(_ % Test) ++
        Dependencies.logs.map(_ % Test)
  )
  .dependsOn(migration)

lazy val root = (project in file("."))
  .aggregate(application, migration, testcontainers)
  .settings(
    name := "cassandra-migration-root",
    crossScalaVersions := Nil,
    publish / skip := true
  )

// based on https://gist.github.com/tabdulradi/aa7450921756cd22db6d278100b2dac8
lazy val scalacOptions213 = Seq(
  "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
  "-encoding", "utf-8",                // Specify character encoding used by source files.
  "-explaintypes",                     // Explain type errors in more detail.
  "-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
  "-language:existentials",            // Existential types (besides wildcard types) can be written and inferred
//  "-language:experimental.macros",   // Allow macro definition (besides implementation and application). Disabled, as this will significantly change in Scala 3
  "-language:higherKinds",             // Allow higher-kinded types
//  "-language:implicitConversions",   // Allow definition of implicit functions called views. Disabled, as it might be dropped in Scala 3. Instead use extension methods (implemented as implicit class Wrapper(val inner: Foo) extends AnyVal {}
  "-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit",                       // Wrap field accessors to throw an exception on uninitialized access.
  "-Xfatal-warnings",                  // Fail the compilation if there are any warnings.

  "-Xlint:adapted-args",               // Warn if an argument list is modified to match the receiver.
  "-Xlint:constant",                   // Evaluation of a constant arithmetic expression results in an error.
  "-Xlint:delayedinit-select",         // Selecting member of DelayedInit.
  "-Xlint:doc-detached",               // A Scaladoc comment appears to be detached from its element.
  "-Xlint:implicit-recursion",
  "-Xlint:inaccessible",               // Warn about inaccessible types in method signatures.
  "-Xlint:infer-any",                  // Warn when a type argument is inferred to be `Any`.
  "-Xlint:missing-interpolator",       // A string literal appears to be missing an interpolator id.
//  "-Xlint:nullary-override",         // 'nullary-override' is not a valid choice for '-Xlint'! Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Xlint:nullary-unit",               // Warn when nullary methods return Unit.
  "-Xlint:option-implicit",            // Option.apply used implicit view.
  "-Xlint:package-object-classes",     // Class or object defined in package object.
  "-Xlint:poly-implicit-overload",     // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow",             // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align",                // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow",      // A local type parameter shadows a type already in scope.
  "-Xlint:unused",                     // TODO check if we still need -Wunused below
  "-Xlint:nonlocal-return",            // A return statement used an exception for flow control.
  "-Xlint:implicit-not-found",         // Check @implicitNotFound and @implicitAmbiguous messages.
  "-Xlint:serial",                     // @SerialVersionUID on traits and non-serializable classes.
  "-Xlint:valpattern",                 // Enable pattern checks in val definitions.
  "-Xlint:eta-zero",                   // Warn on eta-expansion (rather than auto-application) of zero-ary method.
  "-Xlint:eta-sam",                    // Warn on eta-expansion to meet a Java-defined functional interface that is not explicitly annotated with @FunctionalInterface.
  "-Xlint:deprecation",                // Enable linted deprecations.

  "-Wdead-code",                       // Warn when dead code is identified.
  "-Wextra-implicit",                  // Warn when more than one implicit parameter section is defined.
  "-Wmacros:both",                     // Lints code before and after applying a macro
  "-Wnumeric-widen",                   // Warn when numerics are widened.
  "-Woctal-literal",                   // Warn on obsolete octal syntax.
//  "-Wself-implicit",                 // -Wself-implicit is deprecated: Use -Xlint:implicit-recursion! Warn when an implicit resolves to an enclosing self-definition.
  "-Wunused:imports",                  // Warn if an import selector is not referenced.
  "-Wunused:patvars",                  // Warn if a variable bound in a pattern is unused.
  "-Wunused:privates",                 // Warn if a private member is unused.
  "-Wunused:locals",                   // Warn if a local definition is unused.
  "-Wunused:explicits",                // Warn if an explicit parameter is unused.
  "-Wunused:implicits",                // Warn if an implicit parameter is unused.
  "-Wunused:params",                   // Enable -Wunused:explicits,implicits.
  "-Wunused:linted",
  "-Wvalue-discard",                   // Warn when non-Unit expression results are unused.

  "-Ybackend-parallelism", "8",                 // Enable paralellisation — change to desired number!
  "-Ycache-plugin-class-loader:last-modified",  // Enables caching of classloaders for compiler plugins
  "-Ycache-macro-class-loader:last-modified"    // and macro definitions. This can lead to performance improvements.
)

// based on https://tpolecat.github.io/2017/04/25/scalac-flags.html
lazy val scalacOptions212 = Seq(
  "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
  "-encoding", "utf-8",                // Specify character encoding used by source files.
  "-explaintypes",                     // Explain type errors in more detail.
  "-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
  "-language:existentials",            // Existential types (besides wildcard types) can be written and inferred
  "-language:experimental.macros",     // Allow macro definition (besides implementation and application)
  "-language:higherKinds",             // Allow higher-kinded types
  "-language:implicitConversions",     // Allow definition of implicit functions called views
  "-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit",                       // Wrap field accessors to throw an exception on uninitialized access.
  "-Xfatal-warnings",                  // Fail the compilation if there are any warnings.
  "-Xfuture",                          // Turn on future language features.
  "-Xlint:adapted-args",               // Warn if an argument list is modified to match the receiver.
  "-Xlint:by-name-right-associative",  // By-name parameter of right associative operator.
  "-Xlint:constant",                   // Evaluation of a constant arithmetic expression results in an error.
  "-Xlint:delayedinit-select",         // Selecting member of DelayedInit.
  "-Xlint:doc-detached",               // A Scaladoc comment appears to be detached from its element.
  "-Xlint:inaccessible",               // Warn about inaccessible types in method signatures.
  "-Xlint:infer-any",                  // Warn when a type argument is inferred to be `Any`.
  "-Xlint:missing-interpolator",       // A string literal appears to be missing an interpolator id.
  "-Xlint:nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Xlint:nullary-unit",               // Warn when nullary methods return Unit.
  "-Xlint:option-implicit",            // Option.apply used implicit view.
  "-Xlint:package-object-classes",     // Class or object defined in package object.
  "-Xlint:poly-implicit-overload",     // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow",             // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align",                // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow",      // A local type parameter shadows a type already in scope.
  "-Xlint:unsound-match",              // Pattern match may not be typesafe.
  "-Yno-adapted-args",                 // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
  "-Ypartial-unification",             // Enable partial unification in type constructor inference
  "-Ywarn-dead-code",                  // Warn when dead code is identified.
  "-Ywarn-extra-implicit",             // Warn when more than one implicit parameter section is defined.
  "-Ywarn-inaccessible",               // Warn about inaccessible types in method signatures.
  "-Ywarn-infer-any",                  // Warn when a type argument is inferred to be `Any`.
  "-Ywarn-nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Ywarn-nullary-unit",               // Warn when nullary methods return Unit.
  "-Ywarn-numeric-widen",              // Warn when numerics are widened.
  "-Ywarn-unused:implicits",           // Warn if an implicit parameter is unused.
  "-Ywarn-unused:imports",             // Warn if an import selector is not referenced.
  "-Ywarn-unused:locals",              // Warn if a local definition is unused.
  "-Ywarn-unused:params",              // Warn if a value parameter is unused.
  "-Ywarn-unused:patvars",             // Warn if a variable bound in a pattern is unused.
  "-Ywarn-unused:privates",            // Warn if a private member is unused.
  "-Ywarn-value-discard"               // Warn when non-Unit expression results are unused.
)

