name := "documentation"
organization := "itsmeijers"
version := "0.1"
scalaVersion := "2.11.8"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

val akkaVersion = "2.4.2"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor"     % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit"   % akkaVersion % "test",
  "org.scalatest"     %% "scalatest"      % "2.2.6" % "test",
  "net.ruippeixotog"  %% "scala-scraper"  % "0.1.2",
  "ch.qos.logback"    % "logback-classic" % "1.0.9",
  "org.typelevel"     %% "cats"           % "0.4.1")
