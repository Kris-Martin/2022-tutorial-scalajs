lazy val root = (project in file("."))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "Scala.js tutorial",
    version := "2022.1",
    scalaVersion := "3.1.2", 

    scalaJSUseMainModuleInitializer := true,

    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.2.0",

    libraryDependencies += "org.scalameta" %%% "munit" % "0.7.29" % Test
  )

testFrameworks += new TestFramework("munit.Framework")
