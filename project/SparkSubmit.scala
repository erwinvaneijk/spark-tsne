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
  SparkSubmitSetting("sparkMNISTDF",
    Seq(
      "--master", "local[*]",
      "--class", "com.github.saurfang.spark.tsne.examples.MNISTDF"
    )
  )
)
}
