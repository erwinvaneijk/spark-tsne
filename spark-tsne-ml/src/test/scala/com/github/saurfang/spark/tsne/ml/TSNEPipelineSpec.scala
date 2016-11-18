package com.github.saurfang.spark.tsne.ml

import org.apache.spark.SharedSparkContext
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.SparkSession
import org.scalatest.{FlatSpec, Matchers, Outcome}

/**
  * Test the inclusion of tSNE in a Spark Pipeline.
  *
  * Created by Erwin van Eijk on 11/11/16.
  */
class TSNEPipelineSpec extends FlatSpec with SharedSparkContext with Matchers {

  val spark = SparkSession.builder().appName("Spark tSNE Test").getOrCreate()

  val training = spark.createDataFrame(
    Seq(
      (1.0, Vectors.dense(0.0, 1.1, 0.1)),
      (0.0, Vectors.dense(2.0, 1.0, -1.0)),
      (0.0, Vectors.dense(2.0, 1.3, 1.0)),
      (1.0, Vectors.dense(0.0, 1.2, -0.5))
    )
  ).toDF("label", "features")

  "TSNE" should "be compatible to a pipeline" in {
    val tsne = new TSNE("TSNE")
    tsne should not be (null)
  }
}
