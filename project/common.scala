package ckit

import sbt._
import Keys._

package object build {
  val commonSettings = Seq (
    organization := "com.github.wookietreiber",
    scalaVersion := "2.11.1",
    sourceDirectory <<= baseDirectory(identity)
  )
}
