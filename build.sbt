//lazy val repositoryUrl = "git:https://github.com/epfldata/squall"
lazy val repositoryUrl = "git:file:///home/akathorn/Squall/squall/#squallcontext"

lazy val squallCoreRepo = ProjectRef(uri(repositoryUrl), "squall")
lazy val squallFunctionalRepo = ProjectRef(uri(repositoryUrl), "functional")

lazy val root = (Project("root", file(".")) dependsOn(squallCoreRepo, squallFunctionalRepo)).
  settings(
    scalaVersion := "2.11.6",
    name := "squall-quickstart",
    organization := "ch.epfl.data",
    version := "0.1",
    // Set up the Squall console
    libraryDependencies += "org.twitter4j" % "twitter4j-stream" % "3.0.3",
    fullClasspath in (Compile, console) in squallFunctionalRepo ++= (fullClasspath in console in Compile).value,
    console in Compile <<= (console in Compile in squallFunctionalRepo)
  )
