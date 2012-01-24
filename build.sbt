

name := "Scrawler"

version := "1.0"

scalaVersion := "2.9.1"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "se.scalablesolutions.akka" % "akka-actor" % "1.3-RC6"

libraryDependencies += "se.scalablesolutions.akka" % "akka-remote" % "1.3-RC6"

libraryDependencies ++= Seq(
  "net.databinder" %% "dispatch-http" % "0.8.7"
)

libraryDependencies ++= Seq(
  "net.databinder" %% "dispatch-jsoup" % "0.8.7"
)