import ckit.build._
import Dependencies._
import AssemblyKeys._

import akka.sbt.AkkaKernelPlugin
import akka.sbt.AkkaKernelPlugin.{ Dist, outputDirectory, distJvmOptions, distMainClass }

lazy val root = (
  CkitProject("ckit", ".")
  aggregate(core, daemon, clientCore, clientSwing, rest)
)

lazy val core = (
  CkitProject("ckit-core", "core")
  settings(
    libraryDependencies ++= Seq ( actor, remote, time, specs % "test" )
  )
)

lazy val daemon = (
  CkitProject("ckit-daemon", "daemon")
  dependsOn(core)
  settings((AkkaKernelPlugin.distSettings ++ Seq (
    libraryDependencies ++= Seq ( kernel, slf4j, logger, specs % "test" ),
    distJvmOptions in Dist := "-Xms512M -Xmx2048M -Xss1M -XX:MaxPermSize=512M -XX:+UseParallelGC",
    distMainClass in Dist := "akka.kernel.Main ckit.daemon.ClusterKitDaemonKernel",
    outputDirectory in Dist <<= target / "dist"
  )): _*)
)

lazy val clientCore = (
  CkitProject("ckit-client-core", "client/core")
  dependsOn(core)
)

lazy val clientSwing = (
  CkitProject("ckit-client-swing", "client/swing")
  dependsOn(clientCore)
  settings(
    fork in run := true,
    libraryDependencies ++= Seq(chart)
  )
)

lazy val rest = (
  CkitProject("ckit-rest", "rest")
  dependsOn(daemon)
  settings(assemblySettings: _*)
  settings(
    fork in run := true,
    resolvers += "spray" at "http://repo.spray.io/",
    libraryDependencies ++= Seq (actor, slf4j, logger,
      "io.spray" %  "spray-can"     % "1.2.0",
      "io.spray" %  "spray-routing" % "1.2.0",
      "io.spray" %% "spray-json"    % "1.2.5"
    )
  )
)
