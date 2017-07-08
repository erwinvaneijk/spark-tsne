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


import java.io.{BufferedWriter, OutputStreamWriter}

import com.github.saurfang.spark.tsne.impl._
import com.github.saurfang.spark.tsne.tree.SPTree
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.SparkConf
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.linalg.distributed.RowMatrix
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.slf4j.LoggerFactory

object MNIST {
  private def logger = LoggerFactory.getLogger(MNIST.getClass)

  def main (args: Array[String]) {
    val conf = new SparkConf()
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .registerKryoClasses(Array(classOf[SPTree]))
    val spark = SparkSession.builder().appName("MNINST").master("local[*]").config(conf) .getOrCreate()
    val sc = spark.sparkContext
    val hadoopConf = sc.hadoopConfiguration
    val fs = FileSystem.get(hadoopConf)

    val ds = spark.read.csv("data/mnist/mnist.csv.gz").withColumn("id", monotonically_increasing_id())
    val toDouble = udf( (i: Int)=> i.toDouble)
    val arrayColumns = 1.to(786).map{id: Int => toDouble(ds.col(s"_${id}"))}
    val colNames = 1.to(786).map{id: Int => s"_${id}"}
    val dsWithArray = ds.withColumn("v", array(arrayColumns:_*)).drop(colNames:_*)
    dsWithArray.printSchema()
    dsWithArray.show(10)

    val dataset = sc.textFile("data/mnist/mnist.csv.gz")
      .zipWithIndex()
      .filter(_._2 < 6000)
      .sortBy(_._2, ascending = true, 60)
      .map(_._1)
      .map(_.split(","))
      .map(x => (x.head.toInt, x.tail.map(_.toDouble)))
      .cache()
    spark.createDataFrame(dataset).printSchema()

    //logInfo(dataset.collect.map(_._2.toList).toList.toString)

    //val features = dataset.map(x => Vectors.dense(x._2))
    //val scaler = new StandardScaler(true, true).fit(features)
    //val scaledData = scaler.transform(features)
    //  .map(v => Vectors.dense(v.toArray.map(x => if(x.isNaN || x.isInfinite) 0.0 else x)))
    //  .cache()
    val data = dataset.flatMap(_._2)
    val mean = data.mean()
    val std = data.stdev()
    val scaledData = dataset.map(x => Vectors.dense(x._2.map(v => (v - mean) / std))).cache()

    val labels = dataset.map(_._1).collect()
    val matrix = new RowMatrix(scaledData)
    val pcaMatrix = matrix.multiply(matrix.computePrincipalComponents(50))
    pcaMatrix.rows.cache()

    val costWriter = new BufferedWriter(new OutputStreamWriter(fs.create(new Path(s".tmp/MNIST/cost.txt"), true)))

    //SimpleTSNE.tsne(pcaMatrix, perplexity = 20, maxIterations = 200)
    BHTSNE.tsne(pcaMatrix, maxIterations = 500, callback = {
    //LBFGSTSNE.tsne(pcaMatrix, perplexity = 10, maxNumIterations = 500, numCorrections = 10, convergenceTol = 1e-8)
      case (i, y, loss) =>
        if(loss.isDefined) logger.info(s"$i iteration finished with loss $loss")

        val os = fs.create(new Path(s".tmp/MNIST/result${"%05d".format(i)}.csv"), true)
        val writer = new BufferedWriter(new OutputStreamWriter(os))
        try {
          (0 until y.rows).foreach {
            row =>
              writer.write(labels(row).toString)
              writer.write(y(row, ::).inner.toArray.mkString(",", ",", "\n"))
          }
          if(loss.isDefined) costWriter.write(loss.get + "\n")
        } finally {
          writer.close()
        }
    })
    costWriter.close()

    sc.stop()
  }
}
