import sbt._
import Keys._

import BuildSettings._
import Dependencies._
import Resolvers._

object BuildSettings {
  lazy val buildOrganization = "ckit"
  lazy val buildVersion      = "0.1.0-SNAPSHOT"
  lazy val buildScalaVersion = "2.9.1"
  lazy val akkaVersion       = "2.0.1"

  lazy val baseSettings = Defaults.defaultSettings ++ Seq (
    organization   := buildOrganization,
    version        := buildVersion,
    scalaVersion   := buildScalaVersion,
    resolvers     ++= Seq ( sonatype, typesafe )
  )
}

object ClusterKitBuild extends Build {

  lazy val ckit = Project (
    id        = "ckit",
    base      = file ("."),
    aggregate = Seq ( core, daemon, clientCore, clientSwing )
  )

  // -----------------------------------------------------------------------
  // core
  // -----------------------------------------------------------------------

  lazy val core = Project (
    id        = "ckit-core",
    base      = file ("ckit-core"),
    settings  = baseSettings ++ Seq (
      libraryDependencies ++= Seq ( conf, actor, remote, arm, specs2 )
    )
  )

  // -----------------------------------------------------------------------
  // daemon
  // -----------------------------------------------------------------------

  lazy val daemon = Project (
    id            = "ckit-daemon",
    base          = file ("ckit-daemon"),
    dependencies  = Seq ( core ),
    settings      = baseSettings ++ Seq (
      libraryDependencies ++= Seq ( arm, specs2 )
    )
  )

  // -----------------------------------------------------------------------
  // client
  // -----------------------------------------------------------------------

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

  // -----------------------------------------------------------------------
  // compile
  // -----------------------------------------------------------------------

  lazy val swing  = "org.scala-lang"       %  "scala-swing" % buildScalaVersion // Modified BSD (Scala)
  lazy val conf   = "com.typesafe"         %  "config"      % "0.4.0"           // ApacheV2
  lazy val actor  = "com.typesafe.akka"    %  "akka-actor"  % akkaVersion       // ApacheV2
  lazy val remote = "com.typesafe.akka"    %  "akka-remote" % akkaVersion       // ApacheV2
  lazy val time   = "org.scala-tools.time" %% "time"        % "0.5"             // ApacheV2
  lazy val arm    = "com.jsuereth"         %% "scala-arm"   % "1.2"             // Modified BSD (Scala)
  lazy val chart  = "org.jfree"            %  "jfreechart"  % "1.0.14"          // LGPL

  // -----------------------------------------------------------------------
  // test
  // -----------------------------------------------------------------------

  lazy val specs2 = "org.specs2" %% "specs2" % "1.9" % "test"

}

object Resolvers {
  lazy val typesafe = "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases"
  lazy val sonatype = "Sonatype Releases"   at "http://oss.sonatype.org/content/repositories/releases"
}
