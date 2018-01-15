import sbt._

object Dependencies {
  val catsVersion = "1.0.1"
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.3"
  lazy val catsCore = "org.typelevel" %% "cats-core" % catsVersion
  lazy val catsFree = "org.typelevel" %% "cats-free" % catsVersion
}
