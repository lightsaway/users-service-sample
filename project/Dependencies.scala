import sbt.Keys.libraryDependencies
import sbt._

object Dependencies {

  object http4s {
    private val http4sVersion = "0.20.8"
    val server                = "org.http4s" %% "http4s-blaze-server" % http4sVersion
    val client                = "org.http4s" %% "http4s-blaze-client" % http4sVersion
    val circe                 = "org.http4s" %% "http4s-circe" % http4sVersion
    val dsl                   = "org.http4s" %% "http4s-dsl" % http4sVersion
  }

  lazy val newType     = "io.estatico" %% "newtype" % "0.4.3"
  lazy val refinedType = "eu.timepit"  %% "refined" % "0.9.10"

  lazy val scalaTest         = "org.scalatest"      %% "scalatest"              % "3.0.4" % Test
  lazy val testcontainers    = "com.dimafeng"       %% "testcontainers-scala"   % "0.33.0" % Test
  lazy val postgresContainer = "org.testcontainers" % "postgresql"              % "1.12.3" % Test
  lazy val mockito           = "org.mockito"        %% "mockito-scala"          % "1.10.1" % Test
  lazy val mockitoCats       = "org.mockito"        % "mockito-scala-cats_2.12" % "1.10.1"

  lazy val chimney = "io.scalaland" %% "chimney"       % "0.3.5"
  lazy val flyWay  = "org.flywaydb" % "flyway-core"    % "5.2.4"
  lazy val meow    = "com.olegpy"   %% "meow-mtl-core" % "0.4.0"

  object doobie {
    private val version = "0.8.6"
    lazy val core       = "org.tpolecat" %% "doobie-core" % version
    lazy val refined         = "org.tpolecat" %% "doobie-refined" % version
    lazy val pg         = "org.tpolecat" %% "doobie-postgres" % version
    lazy val hikari     = "org.tpolecat" %% "doobie-hikari" % version

  }
  object fs2 {
    private val version = "2.0.0"
    lazy val core       = "co.fs2" %% "fs2-core" % version
    lazy val io         = "co.fs2" %% "fs2-io" % version
  }

  object circe {
    private val circeDerivationVersion = "0.12.0-M7"
    lazy val derivation                = "io.circe" %% "circe-derivation" % circeDerivationVersion
    lazy val annotation                = "io.circe" %% "circe-derivation-annotations" % circeDerivationVersion

    lazy val config          = "io.circe" %% "circe-config" % "0.7.0"
    lazy val yaml            = "io.circe" %% "circe-yaml" % "0.10.0"
    private val circeVersion = "0.12.2"
    lazy val literal         = "io.circe" %% "circe-literal" % circeVersion % Test
    lazy val generic         = "io.circe" %% "circe-generic" % circeVersion
    lazy val extras          = "io.circe" %% "circe-generic-extras" % circeVersion
    lazy val refined         = "io.circe" %% "circe-refined" % circeVersion
    lazy val core            = "io.circe" %% "circe-core" % circeVersion
    lazy val parser          = "io.circe" % "circe-parser" % circeVersion
  }

  object slf4j {
    private val version = "1.7.25"

    val api       = "org.slf4j" % "slf4j-api"        % version
    val log4jOver = "org.slf4j" % "log4j-over-slf4j" % version
  }

  object logback {
    private val version = "1.2.3"

    val core    = "ch.qos.logback" % "logback-core"    % version
    val classic = "ch.qos.logback" % "logback-classic" % version
  }
}
