import Common._

lazy val root = Project("spark-tsne", file(".")).
  settings(commonSettings: _*).
  settings(test in assembly := {}).
  aggregate(core, vis, examples)

lazy val core = tsneProject("spark-tsne-core").
  settings(test in assembly := {}).
  settings(Dependencies.core)

lazy val vis = tsneProject("spark-tsne-player").
  dependsOn(core)

lazy val examples = tsneProject("spark-tsne-examples").
  dependsOn(core, vis).
  settings(fork in run := true).
  settings(test in assembly := {}).
  settings(Dependencies.core).
  settings(SparkSubmit.settings: _*)

