/*
 * Copyright (c) 2017, Forest Fang.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.saurfang.spark.tsne.examples

import com.github.saurfang.spark.tsne.TSNE
import com.github.saurfang.spark.tsne.tree.SPTree
import org.apache.spark.SparkConf
import org.apache.spark.ml.feature.PCA
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

import scala.collection.mutable

/**
  * Created by kojak on 05/02/17.
  */
object MNISTv2 {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .registerKryoClasses(Array(classOf[SPTree]))
    val spark = SparkSession.builder().appName("MNINST").master("local[*]").config(conf) .getOrCreate()
    import spark.implicits._
    // This does not work - the program becomes to large at runtime.
    val dataset = spark.sparkContext.textFile("data/mnist/mnist.csv.gz")
      .zipWithIndex()
      .filter(_._2 < 6000)
      .sortBy(_._2, true, 60)
      .map(_._1)
      .map(_.split(","))
      .map(x => (x.head.toInt, x.tail.map(_.toDouble)))
    def toVector = udf( (v: mutable.WrappedArray[Double]) => Vectors.dense(v.toArray))
    val ds = spark.createDataFrame(dataset).toDF("label", "v").withColumn("vector", toVector($"v") ).drop("v")
    ds.printSchema()
    ds.show(10)

    val pca = new PCA().setInputCol("vector").setOutputCol("pca").setK(50)
    val pcaModel = pca.fit(ds)
    val pcaData = pcaModel.transform(ds)
    pcaData.printSchema()
    pcaData.show(10)

    val tsne = new TSNE().setInputCol("pca").setOutputCol("dim")
    val transformed = tsne.transform(pcaData)
    transformed.printSchema()
    transformed.show(10)
  }
}
