import sbt._
import Keys._
 
object ReductoBuild extends Build {
  val Organization = "keco1249"
  val Version      = "0.1.0"
  val ScalaVersion = "2.10.3"
 
  lazy val DumbledoreDist = Project(
    id = "Jenkins-Ansible-CI",
    base = file("."),
    settings = defaultSettings
  )
 
  lazy val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := Organization,
    version      := Version,
    scalaVersion := ScalaVersion,
    crossPaths   := false,
    organizationName := "keco1249"
  )
  
  lazy val defaultSettings = buildSettings ++ Seq(
    // compile options
    scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked"),
    javacOptions  ++= Seq("-Xlint:unchecked", "-Xlint:deprecation")
  )
}