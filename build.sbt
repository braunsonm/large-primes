name := "math465-large-primes"

version := "1.0"

scalaVersion := "2.11.12"
scalacOptions ++= Seq("-feature")

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.4.1" % "provided",
  "org.scala-lang.modules" %% "scala-java8-compat" % "0.8.0",
  "org.apache.logging.log4j" % "log4j-core" % "2.13.0"
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
