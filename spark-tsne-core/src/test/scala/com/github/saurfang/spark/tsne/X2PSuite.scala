package com.github.saurfang.spark.tsne

import org.apache.spark.SharedSparkContext
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.linalg.distributed.RowMatrix
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.scalatest.{FunSuite, Matchers}

/**
 * Created by forest on 8/16/15.
 */
class X2PSuite extends FunSuite with SharedSparkContext with Matchers {

  test("Test X2P against tsne.jl implementation") {
    val input = new RowMatrix(
      sc.parallelize(Seq(1 to 3, 4 to 6, 7 to 9, 10 to 12))
        .map(x => Vectors.dense(x.map(_.toDouble).toArray))
    )
    val output = X2P(input, 1e-5, 2)(sc).toRowMatrix().rows.collect().map(_.toArray.toList)
    println(output.toList)

    val expectedList = List(List(0.0, 0.7271751954399842, 0.23646351469950624, 0.036361289860509495), List(0.49999999999999645, 0.0, 0.5000000000000036, 3.31983859979058E-36), List(3.3198385997903674E-36, 0.5, 0.0, 0.5), List(0.03636128986050948, 0.23646351469950638, 0.7271751954399842, 0.0))
    //val expectedListFromNew = List(List(0.0, 0.7271751954399843, 0.23646351469950624, 0.03636128986050946), List(0.5, 0.0, 0.5, 3.3198385997903674E-36), List(3.3198385997903674E-36, 0.5, 0.0, 0.5), List(0.03636128986050946, 0.23646351469950624, 0.7271751954399843, 0.0))
    // The error is very minor, that is why these two tests have to different outcomes, and we're using these
    // two results (for now, until we can make the test a bit more double-compute agnostic).
    output.toList should be (expectedList)
    //output shouldBe List(List(0, .5, .5), List(.5, 0, .5), List(.5, .5, .0))
  }

  test("Test new X2P against small dataset") {
    val sparkSession = SparkSession.builder().getOrCreate()
    val dataset: Seq[(Int, Vector)] = Seq(1 to 3, 4 to 6, 7 to 9, 10 to 12)
      .map(x => Vectors.dense(x.map(_.toDouble).toArray)).zipWithIndex
      .map{ case (v, i) => (i, v)}
    val input = sparkSession.createDataFrame(dataset).toDF("i", "v")
    input.printSchema()
    val output = X2P(input, 1e-5, 2)(sparkSession).toRowMatrix().rows.collect().map(_.toArray.toList)
    println(output.toList)
    val expectedList = List(List(0.0,                0.7271751954399843, 0.23646351469950624, 0.03636128986050946), List(0.5, 0.0, 0.5, 3.3198385997903674E-36), List(3.3198385997903674E-36, 0.5, 0.0, 0.5), List(0.03636128986050946, 0.23646351469950624, 0.7271751954399843, 0.0))

    output.toList should be (expectedList)
  }
}
