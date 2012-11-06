import sbt._
import Keys._

import BuildSettings._
import Dependencies._

object BuildSettings {
  lazy val buildOrganization = "ckit"
  lazy val buildVersion      = "0.1.0-SNAPSHOT"
  lazy val buildScalaVersion = "2.10.0-RC1"
  lazy val akkaVersion       = "2.1.0-RC1"
  lazy val latest            = "latest.integration"

  lazy val baseSettings = Defaults.defaultSettings ++ Seq (
    organization   := buildOrganization,
    version        := buildVersion,
    scalaVersion   := buildScalaVersion
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
    id        = "ckit-core",
    base      = file ("ckit-core"),
    settings  = baseSettings ++ Seq (
      libraryDependencies ++= Seq ( conf, actor, remote, arm, specs2 )
    )
  )

  // -----------------------------------------------------------------------------------------------
  // daemon
  // -----------------------------------------------------------------------------------------------

  lazy val daemon = Project (
    id            = "ckit-daemon",
    base          = file ("ckit-daemon"),
    dependencies  = Seq ( core ),
    settings      = baseSettings ++ Seq (
      libraryDependencies ++= Seq ( arm, specs2 )
    )
  )

  // -----------------------------------------------------------------------------------------------
  // client
  // -----------------------------------------------------------------------------------------------

  lazy val clientCore = Project (
    id            = "ckit-client-core",
    base          = file ("ckit-client-core"),
    dependencies  = Seq ( core ),
    settings      = baseSettings
  )

  lazy val clientSwing = Project (
    id            = "ckit-client-swing",
    base          = file ("ckit-client-swing"),
    dependencies  = Seq ( clientCore ),
    settings      = baseSettings ++ Seq (
      libraryDependencies ++= Seq ( swing, chart )
    )
  )

}

object Dependencies {

  // -----------------------------------------------------------------------------------------------
  // compile
  // -----------------------------------------------------------------------------------------------

  lazy val swing  = "org.scala-lang"       %  "scala-swing" % buildScalaVersion                   // Modified BSD (Scala)
  lazy val conf   = "com.typesafe"         %  "config"      % "1.0.0"                             // ApacheV2
  lazy val actor  = "com.typesafe.akka"    %  "akka-actor"  % akkaVersion cross CrossVersion.full // ApacheV2
  lazy val remote = "com.typesafe.akka"    %  "akka-remote" % akkaVersion cross CrossVersion.full // ApacheV2
  lazy val time   = "org.scala-tools.time" %% "time"        % "0.5"                               // ApacheV2
  lazy val arm    = "com.jsuereth"         %  "scala-arm"   % "1.2" cross CrossVersion.full       // Modified BSD (Scala)
  lazy val chart  = "org.sfree"            %% "sfreechart"  % latest                              // LGPL

  // -----------------------------------------------------------------------------------------------
  // test
  // -----------------------------------------------------------------------------------------------

  lazy val specs2 = "org.specs2" % "specs2" % "1.12.2" % "test" cross CrossVersion.full

}
