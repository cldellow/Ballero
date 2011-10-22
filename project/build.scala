import sbt._

import Keys._
import AndroidKeys._

object General {
  val settings = Defaults.defaultSettings ++ Seq (
    name := "Ballero",
    version := "0.1",
    scalaVersion := "2.8.1",
    platformName in Android := "android-8"
  )

  lazy val fullAndroidSettings =
    General.settings ++
    AndroidProject.androidSettings ++
    TypedResources.settings ++
    AndroidMarketPublish.settings ++ Seq (
      keyalias in Android := "change-me",
      resolvers ++= Seq("Coda Hales Repository" at "http://repo.codahale.com"),
      libraryDependencies ++= Seq("org.scalatest" %% "scalatest" % "1.5.1" % "test",
        "com.codahale" % "jerkson_2.8.2" % "0.5.0")
    )
}

object AndroidBuild extends Build {
  lazy val main = Project (
    "Ballero",
    file("."),
    settings = General.fullAndroidSettings
  )

  lazy val tests = Project (
    "tests",
    file("tests"),
    settings = General.settings ++ AndroidTest.androidSettings
  ) dependsOn main
}
