import Common._

wartremoverErrors in (Compile, compile) ++= Warts.unsafe

lazy val root = Project("spark-tsne", file(".")).
  settings(commonSettings: _*).
  aggregate(core, ml, vis, examples)

lazy val core = tsneProject("spark-tsne-core").
  settings(Dependencies.core)

lazy val ml = tsneProject("spark-tsne-ml").
  dependsOn(core).
  settings(Dependencies.core)

lazy val vis = tsneProject("spark-tsne-player").
  dependsOn(core)

lazy val examples = tsneProject("spark-tsne-examples").
  dependsOn(core, vis).
  settings(fork in run := true).
  settings(Dependencies.core).
  settings(SparkSubmit.settings: _*)
