ThisBuild / scalaVersion := "2.13.1"

ThisBuild / libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"

ThisBuild / libraryDependencies +=
  "com.typesafe.akka" %% "akka-stream" % "2.6.14"

ThisBuild / libraryDependencies +=
  "com.typesafe.akka" %% "akka-actor" % "2.6.14"
