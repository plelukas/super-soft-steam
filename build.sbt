lazy val nameSettings = Seq(
  organization := "com.synerise",
  name := "super-soft-steam",
  scalaVersion := "2.12.10",
)

version := "0.0.1-SNAPSHOT"
val akkaVersion = "2.5.23"
val akkaHttpVersion = "10.1.9"
val akkaManagementVersion = "1.0.3"
val kafkaVersion = "1.1.0"

// PROJECTS

lazy val SuperSoftSteam = project
  .in(file("."))
  .settings(settings)
  .disablePlugins(AssemblyPlugin)
  .aggregate(
    common,
    master,
    worker
  )

lazy val common = project
  .settings(
    name := "common",
    settings,
    libraryDependencies ++= commonDependencies
  )
  .disablePlugins(AssemblyPlugin)

lazy val master = project
  .settings(
    name := "master",
    mainClass := Some("com.synerise.steam.master.Master"),
    settings,
    assemblySettings,
    libraryDependencies ++= commonDependencies ++ Seq()
  )
  .dependsOn(
    common
  )

lazy val worker = project
  .settings(
    name := "worker",
    mainClass := Some("com.synerise.steam.worker.Worker"),
    settings,
    assemblySettings,
    libraryDependencies ++= commonDependencies ++ Seq()
  )
  .dependsOn(
    common
  )

//DEPENDENCIES


lazy val commonDependencies = Seq(
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
//  "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion, need akka 2.6.0
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-kafka" % kafkaVersion,

  "com.typesafe.akka" %% "akka-discovery" % akkaVersion,

  "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % akkaManagementVersion,
  "com.lightbend.akka.management" %% "akka-management-cluster-http" % akkaManagementVersion,
  "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % akkaManagementVersion,
)

// SETTINGS

lazy val settings =
  commonSettings

lazy val compilerOptions = Seq(
  "-unchecked",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-deprecation",
  "-encoding",
  "utf8"
)

lazy val commonSettings = Seq(
  scalacOptions ++= compilerOptions,
  resolvers += "Artifactory" at "http://artifactory.service/sbt-release/",
  resolvers += "Artifactory Dev" at "http://artifactory.service/sbt-dev",
  resolvers += "confluent" at "https://packages.confluent.io/maven/",
  resolvers += Resolver.bintrayRepo("tanukkii007", "maven"),
)

lazy val assemblySettings = Seq(
  mainClass in assembly := mainClass.value,
  assemblyJarName in assembly := name.value + ".jar",
  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", xs@_*) =>
      (xs map {
        _.toLowerCase
      }) match {
        case ("aop.xml" :: Nil) =>
          MergeStrategy.concat
        case ("manifest.mf" :: Nil) =>
          MergeStrategy.discard
        case _ =>
          MergeStrategy.concat
      }
    case PathList("com", "twitter", "zipkin", _*) => MergeStrategy.last
    case PathList("javax", "annotation", _*) => MergeStrategy.first
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  }
)
