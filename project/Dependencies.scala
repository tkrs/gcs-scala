import sbt._

object Dependencies {
  val Ver = new {
    val `scala2.12`               = "2.12.4"
    val `scala2.11`               = "2.11.12"
    val scalafmt                  = "1.2.0"
    val cats                      = "1.0.1"
    val scalacheck                = "1.13.5"
    val scalatest                 = "3.0.4"
    val googleCloudStorageVersion = "1.15.0"
  }

  val Pkg = new {
    lazy val googleCloudStorage = "com.google.cloud" % "google-cloud-storage" % Ver.googleCloudStorageVersion
    lazy val catsCore           = "org.typelevel"    %% "cats-core"           % Ver.cats
    lazy val scalatest          = "org.scalatest"    %% "scalatest"           % Ver.scalatest
    lazy val scalacheck         = "org.scalacheck"   %% "scalacheck"          % Ver.scalacheck

    lazy val forTest = Seq(scalatest, scalacheck).map(_ % "test")
  }
}
