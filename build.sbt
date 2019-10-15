name := "scala-sandbox"

version := "0.1"

scalaVersion := "2.12.8"
scalacOptions += "-Ypartial-unification"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "1.6.0",
  "org.typelevel" %% "cats-effect" % "1.3.1",
  "com.typesafe.akka" %% "akka-actor-typed" % "2.5.23",
  "com.typesafe.akka" %% "akka-persistence-typed" % "2.5.23",
  "com.typesafe.akka" %% "akka-persistence-cassandra" % "0.99",
  "com.typesafe.akka" %% "akka-cluster-sharding-typed" % "2.5.23",
  "com.typesafe.akka" %% "akka-stream-kafka" % "1.0.4",
  "com.typesafe.akka" %% "akka-stream-contrib" % "0.10",
  "com.lightbend.akka" %% "akka-stream-alpakka-amqp" % "1.1.1",
  "com.davegurnell" %% "checklist" % "0.5.0",
  "com.lihaoyi" %% "pprint" % "0.5.5"

)