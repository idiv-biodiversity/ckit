package ckit
package build

import sbt._

object Dependencies {
  val AkkaV = "2.3.5"

  val actor  = "com.typesafe.akka"        %% "akka-actor"      % AkkaV
  val remote = "com.typesafe.akka"        %% "akka-remote"     % AkkaV
  val kernel = "com.typesafe.akka"        %% "akka-kernel"     % AkkaV
  val slf4j  = "com.typesafe.akka"        %% "akka-slf4j"      % AkkaV

  def Spray = Seq (
    "io.spray" %% "spray-can"     % "1.3.1",
    "io.spray" %% "spray-routing" % "1.3.1",
    "io.spray" %% "spray-json"    % "1.2.6"
  )

  val arm    = "com.jsuereth"             %% "scala-arm"       % "1.2"
  val chart  = "com.github.wookietreiber" %% "scala-chart"     % "0.4.2"
  val logger = "ch.qos.logback"           %  "logback-classic" % "1.1.2"
  val specs  = "org.specs2"               %% "specs2-core"     % "2.4.1"
  val time   = "com.github.nscala-time"   %% "nscala-time"     % "1.4.0"
  val xmlMod = "org.scala-lang.modules"   %% "scala-xml"       % "1.0.2"
}
