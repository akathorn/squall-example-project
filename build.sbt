// Squall dependencies
lazy val squallVersion = "8ae151f1463406294470e533f9e11904458522b9"
lazy val repositoryUrl = "git:https://github.com/akathorn/squall#" + squallVersion
lazy val squallCoreRepo = ProjectRef(uri(repositoryUrl), "squall")
lazy val squallFunctionalRepo = ProjectRef(uri(repositoryUrl), "functional")

lazy val root = (Project("root", file(".")) dependsOn(squallCoreRepo, squallFunctionalRepo)).
  settings(
    // Project settings
    scalaVersion := "2.11.6",
    name := "squall-example-project",
    organization := "ch.epfl.data",
    version := "0.1",
    // Set up the Squall console
    libraryDependencies += "org.twitter4j" % "twitter4j-stream" % "3.0.3",
    fullClasspath in (Compile, console) in squallFunctionalRepo ++= (fullClasspath in console in Compile).value,
    console in Compile <<= (console in Compile in squallFunctionalRepo)
  )
