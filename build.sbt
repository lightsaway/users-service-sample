import Dependencies._

enablePlugins(PackPlugin)

packMain := Map("run-service.sh" -> "vidIq.reqres.Main")

lazy val root = (project in file("."))
  .settings(
    organization := "vidIq",
    name := "reqres",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.10",
    libraryDependencies ++= Seq(
      circe.yaml,
      circe.generic,
      circe.literal,
      circe.extras,
      meow,
      testcontainers,
      mockito,
      postgresContainer,
      doobie.pg,
      doobie.core,
      doobie.hikari,
      flyWay,
      chimney,
      circe.core,
      http4s.dsl,
      http4s.client,
      http4s.server,
      http4s.circe,
      scalaTest,
      logback.classic
    ),
    addCompilerPlugin("org.typelevel"   %% "kind-projector"     % "0.10.3"),
    addCompilerPlugin("com.olegpy"      %% "better-monadic-for" % "0.3.0"),
    addCompilerPlugin("org.scalamacros" % "paradise"            % "2.1.1" cross CrossVersion.full)
  )

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-language:implicitConversions",
  "-feature",
  "-Ypartial-unification",
  "-Xfatal-warnings"
)
