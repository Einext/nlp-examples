name := "IMDBCommentAnalysis"

version := "1.0"

scalaVersion := "2.11.8"


libraryDependencies ++= Seq(
  "edu.stanford.nlp" % "stanford-corenlp" % "3.7.0" artifacts (Artifact("stanford-corenlp", "models"), Artifact("stanford-corenlp")),
  "com.typesafe.play" %% "play-json" % "2.5.13",
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "org.slf4j" % "slf4j-log4j12" % "1.7.25"
)