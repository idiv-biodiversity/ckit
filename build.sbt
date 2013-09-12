import ckit.build._
import Dependencies._

import akka.sbt.AkkaKernelPlugin
import akka.sbt.AkkaKernelPlugin.{ Dist, outputDirectory, distJvmOptions, distMainClass }

lazy val root = (
  CkitProject("ckit", ".")
  aggregate(core, daemon, clientCore, clientSwing)
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
    libraryDependencies ++= Seq(chart, chart)
  )
)
