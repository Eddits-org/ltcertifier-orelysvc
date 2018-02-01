name := "eth-kyc-orely-svc"

organization := "lu.intech"

version := "0.1"

scalaVersion := "2.12.4"

fork in run := false

val luxtrustDependencies = Seq(
  "org.codehaus.groovy" % "groovy" % "2.4.6" classifier "indy",
  "org.codehaus.groovy" % "groovy-xml" % "2.4.6" classifier "indy",
  "joda-time" % "joda-time" % "2.9",
  "org.opensaml" % "opensaml" % "2.6.4",
  "org.apache.commons" % "commons-lang3" % "3.1",
  "javax.activation" % "activation" % "1.1.1"
)

libraryDependencies ++= Seq(
  "com.twitter" %% "finatra-http" % "17.11.0",
  "com.typesafe" % "config" % "1.3.2",
  "org.apache.santuario" % "xmlsec" % "2.1.0"
) ++ luxtrustDependencies

unmanagedJars in Compile ++= Seq(
  file("lib/OrelySamlApi-1.1.jar"),
  file("lib/dss-4.0.0.jar"),
  file("lib/dss-client-4.0.0.jar")
)

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-encoding", "UTF-8", 
  "-feature",
  "-language:postfixOps"
)


assemblyMergeStrategy in assembly := {
  case PathList("BUILD", _ @ _*) => MergeStrategy.discard
  case PathList("rootdoc.txt", _ @ _*) => MergeStrategy.concat
  case PathList("META-INF", "io.netty.versions.properties", _ @ _*) => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
