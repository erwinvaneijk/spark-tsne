import sbtsparksubmit.SparkSubmitPlugin.autoImport._

object SparkSubmit {
  lazy val settings =
    SparkSubmitSetting(
      SparkSubmitSetting("sparkMNIST",
        Seq(
          "--master", "local[3]",
          "--class", "com.github.saurfang.spark.tsne.examples.MNIST"
        )
      ),
      SparkSubmitSetting("v2",
        Seq(
          "--master", "local[*]",
          "--class", "com.github.saurfang.spark.tsne.examples.MNISTv2"
        )
      )
    )
}
