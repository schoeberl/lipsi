name := "Lipsi"

scalaVersion := "2.12.16"
  
scalacOptions := Seq("-Xsource:2.11")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:reflectiveCalls")

libraryDependencies += scalaVersion("org.scala-lang" % "scala-compiler" % _).value

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases")
)

// here switch between Chisel 2 and 3

//libraryDependencies += "edu.berkeley.cs" %% "chisel" % "2.2.38"

//libraryDependencies += "edu.berkeley.cs" %% "chisel3" % "3.1.3"
libraryDependencies += "edu.berkeley.cs" %% "chisel-iotesters" % "1.4.0"
