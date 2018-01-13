import Dependencies._


lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.4",
      version := "0.1.0-SNAPSHOT"
    )),
    name := "Hello",
    resolvers += Resolver.sonatypeRepo("releases"),
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4"),
    libraryDependencies += "org.typelevel" %% "cats-core" % "1.0.1",
    libraryDependencies += scalaTest % Test
  )
