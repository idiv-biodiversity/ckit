organization in ThisBuild := "com.github.wookietreiber"

scalaVersion in ThisBuild := "2.11.2"

autoAPIMappings in ThisBuild := true

resolvers in ThisBuild += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"

resolvers in ThisBuild += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
