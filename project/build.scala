import sbt._
import Keys._

import akka.sbt.AkkaKernelPlugin
import akka.sbt.AkkaKernelPlugin.{ Dist, outputDirectory, distJvmOptions, distMainClass }

import BuildSettings._
import Dependencies._

object BuildSettings {
  lazy val Organization = "ckit"
  lazy val Version      = "0.1.0-SNAPSHOT"
  lazy val ScalaV       = "2.10.0-RC2"
  lazy val AkkaV        = "2.1.0-RC2"
  lazy val Latest       = "latest.integration"

  lazy val baseSettings = Defaults.defaultSettings ++ Seq (
    organization   := Organization,
    version        := Version,
    scalaVersion   := ScalaV
  )
}

object ClusterKitBuild extends Build {

  // -----------------------------------------------------------------------------------------------
  // aggregate
  // -----------------------------------------------------------------------------------------------

  lazy val ckit = Project (
    id        = "ckit",
    base      = file ("."),
    settings  = baseSettings,
    aggregate = Seq ( core, daemon, clientCore, clientSwing )
  )

  // -----------------------------------------------------------------------------------------------
  // core - shared by both daemon and clients
  // -----------------------------------------------------------------------------------------------

  lazy val core = Project (
    id        = "core",
    base      = file ("core"),
    settings  = baseSettings ++ Seq (
      name := "ckit-core",
      libraryDependencies ++= Seq ( actor, remote )
    )
  )

  // -----------------------------------------------------------------------------------------------
  // daemon
  // -----------------------------------------------------------------------------------------------

  lazy val daemon = Project (
    id            = "daemon",
    base          = file ("daemon"),
    dependencies  = Seq ( core ),
    settings      = baseSettings ++ AkkaKernelPlugin.distSettings ++ Seq (
      name := "ckit-daemon",
      libraryDependencies ++= Seq ( kernel, slf4j, logger, specs2 ),
      distJvmOptions in Dist := "-Xms512M -Xmx2048M -Xss1M -XX:MaxPermSize=512M -XX:+UseParallelGC",
      distMainClass in Dist := "akka.kernel.Main ckit.daemon.ClusterKitDaemonKernel",
      outputDirectory in Dist <<= target / "dist"
    )
  )

  // -----------------------------------------------------------------------------------------------
  // client
  // -----------------------------------------------------------------------------------------------

  lazy val clientCore = Project (
    id            = "client-core",
    base          = file ("client-core"),
    dependencies  = Seq ( core ),
    settings      = baseSettings ++ Seq (
      name := "ckit-client-core"
    )
  )

  lazy val clientSwing = Project (
    id            = "client-swing",
    base          = file ("client-swing"),
    dependencies  = Seq ( clientCore ),
    settings      = baseSettings ++ Seq (
      name := "ckit-client-swing",
      libraryDependencies ++= Seq ( swing, chart )
    )
  )

}

object Dependencies {

  // -----------------------------------------------------------------------------------------------
  // compile
  // -----------------------------------------------------------------------------------------------

  lazy val swing  = "org.scala-lang"       %  "scala-swing"     % ScalaV                        // Modified BSD (Scala)
  lazy val actor  = "com.typesafe.akka"    %  "akka-actor"      % AkkaV cross CrossVersion.full // ApacheV2
  lazy val remote = "com.typesafe.akka"    %  "akka-remote"     % AkkaV cross CrossVersion.full // ApacheV2
  lazy val kernel = "com.typesafe.akka"    %  "akka-kernel"     % AkkaV cross CrossVersion.full // ApacheV2
  lazy val slf4j  = "com.typesafe.akka"    %  "akka-slf4j"      % AkkaV cross CrossVersion.full // ApacheV2
  lazy val logger = "ch.qos.logback"       %  "logback-classic" % "1.0.7"                       // LGPL
  lazy val time   = "org.scala-tools.time" %% "time"            % "0.5"                         // ApacheV2
  lazy val arm    = "com.jsuereth"         %  "scala-arm"       % "1.2" cross CrossVersion.full // Modified BSD (Scala)
  lazy val chart  = "org.sfree"            %% "sfreechart"      % Latest                        // LGPL

  // -----------------------------------------------------------------------------------------------
  // test
  // -----------------------------------------------------------------------------------------------

  lazy val specs2 = "org.specs2" % "specs2" % "1.12.2" % "test" cross CrossVersion.full

}
