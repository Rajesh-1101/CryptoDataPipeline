name := "CryptoDataPipeline"

version := "0.1"

scalaVersion := "2.12.14"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.5.0",
  "org.apache.spark" %% "spark-sql" % "3.5.0",
  "org.scalaj" %% "scalaj-http" % "2.4.2"
)