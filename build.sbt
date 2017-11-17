name := "eth-kyc-orely-svc"

organization := "lu.intech"

version := "0.1"

scalaVersion := "2.12.4"

fork in run := false

libraryDependencies ++= Seq(
  "com.twitter" %% "finatra-http" % "17.11.0",
  "org.opensaml" % "opensaml" % "2.6.4",
  "org.apache.commons" % "commons-lang3" % "3.1",
  "com.typesafe" % "config" % "1.3.2",
  "javax.activation" % "activation" % "1.1.1"
)

unmanagedJars in Compile += file("lib/OrelySamlApi-1.1.jar")

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-encoding", "UTF-8", 
  "-feature",
  "-language:postfixOps"
)
