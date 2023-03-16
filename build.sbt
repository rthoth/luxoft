ThisBuild / scalaVersion := "3.2.2"
ThisBuild / organization := "com.luxoft"
ThisBuild / version      := "1.0.0"

lazy val root = (project in file("."))
  .settings(
    name := "task",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio"               % "2.0.10",
      "dev.zio" %% "zio-streams"       % "2.0.10",
      "dev.zio" %% "zio-test"          % "2.0.10" % Test,
      "dev.zio" %% "zio-test-sbt"      % "2.0.10" % Test,
      "dev.zio" %% "zio-test-magnolia" % "2.0.10" % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
  .enablePlugins(JavaAppPackaging)
