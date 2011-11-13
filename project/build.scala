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
      keyalias in Android := "ballero",
      libraryDependencies ++= Seq("org.scalatest" %% "scalatest" % "1.5.1" % "test",
        "org.codehaus.jackson" % "jackson-core-asl" % "1.9.2",
        "com.bugsense" % "trace" % "1.1")
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
