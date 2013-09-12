package ckit
package build

import sbt._

object Dependencies {
  val AkkaV = "2.2.1"

  val actor  = "com.typesafe.akka"        %% "akka-actor"      % AkkaV
  val remote = "com.typesafe.akka"        %% "akka-remote"     % AkkaV
  val kernel = "com.typesafe.akka"        %% "akka-kernel"     % AkkaV
  val slf4j  = "com.typesafe.akka"        %% "akka-slf4j"      % AkkaV

  val arm    = "com.jsuereth"             %% "scala-arm"       % "1.2"
  val chart  = "com.github.wookietreiber" %% "scala-chart"     % "0.2.2"
  val logger = "ch.qos.logback"           %  "logback-classic" % "1.0.13"
  val specs  = "org.specs2"               %% "specs2"          % "2.2"
  val time   = "com.github.nscala-time"   %% "nscala-time"     % "0.6.0"
}
