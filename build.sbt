import Dependencies._


lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.github.yarhrn",
      scalaVersion := "2.12.4",
      version := "0.1.0-SNAPSHOT"
    )),
    name := "contextee",
    resolvers += Resolver.sonatypeRepo("releases"),
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4"),
    libraryDependencies ++= Seq(catsCore, scalaTest % Test)
  )
