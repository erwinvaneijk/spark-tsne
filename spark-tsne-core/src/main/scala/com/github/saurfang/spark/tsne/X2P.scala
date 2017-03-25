package com.github.saurfang.spark.tsne

import breeze.linalg.DenseVector
import org.apache.spark.SparkContext
import org.apache.spark.mllib.X2PHelper._
import org.apache.spark.mllib.linalg.distributed.{CoordinateMatrix, MatrixEntry, RowMatrix}
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.rdd.MLPairRDDFunctions._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.functions.{collect_list, lit, monotonically_increasing_id, udf}
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.slf4j.LoggerFactory

import scala.collection.mutable

object X2P {

  private def logger = LoggerFactory.getLogger(X2P.getClass)

  private def distanceCompute = udf((v: Vector, u: Vector) =>
    Vectors.sqdist(v, u)
  )

  private def toLong = udf((i: java.math.BigDecimal) => i.longValue())

  private def cosineSimilarity = udf((v: Vector, u: Vector) => {
    var dotProduct = 0.0
    var normA = 0.0
    var normB = 0.0
    var index = v.size - 1

    for (i <- 0 to index) {
      dotProduct += v(i) * u(i)
      normA += Math.pow(v(i), 2)
      normB += Math.pow(u(i), 2)
    }
    (dotProduct / (Math.sqrt(normA) * Math.sqrt(normB)))
  }
  )

  private def pairwisePairUp = udf ( (first: mutable.WrappedArray[Long], second: mutable.WrappedArray[Double]) => {
    require(first.length == second.length, "Both sizes should be equal.")

    first.zipWithIndex.map {
      case (num: Long, index: Int) =>
        (num, second(index))
    }
  }
  )

  private def topN(n: Int) = udf ((col: mutable.WrappedArray[GenericRowWithSchema]) => {
    implicit val ordering = Ordering.by{e: (Long, Double)  =>
      -e._2
    }
    val queue = BoundedPriorityQueue[(Long, Double)](n)
    col.foreach { e : GenericRowWithSchema =>
      val l: Long = e.getAs[Long](0)
      val d: Double = e.getAs[Double](1)
      queue += ( (l, d) )
    }

    queue.toArray.sorted(ordering.reverse)
  })

  private def toDistanceMatrix(x: DataFrame)(implicit spark: SparkSession): DataFrame = {
    import spark.implicits._

    // The following is only necessary when there's not another column available with which to
    // identify the right row.
    val rows : DataFrame = x.withColumn("num", toLong(monotonically_increasing_id()))
    val indices: DataFrame = rows.as("A").join(rows.as("B"), lit(true), "outer")
    // Do not compute only the triangle - it will save time later on, expanding the RDD is much more
    // costly on huge datasets.
    val withDistance = indices.withColumn("distance", distanceCompute($"A.v", $"B.v")).withColumn("cosineDistance", cosineSimilarity($"A.v", $"B.v"))
    withDistance.drop($"A.v").drop($"B.v").toDF("i", "i_num", "j", "j_num", "distance", "cosineDistance").filter($"i" =!= $"j")
  }

  /**
    * Only get the mu ranked entries in the resulting DataFrame.
    * @param mu the number of entries to get.
    * @param distanceMatrix the DataFrame to base the analysis on.
    * @return a new DataFrame with a new 'neighbors' column.
    */
  private def computeRankedNeighbors(mu: Int, distanceMatrix: DataFrame)(implicit spark: SparkSession): DataFrame = {
    // Aggregate everything by the first numbered index.
    // Then push the aggregated list through a BoundedPriorityQueue, so we keep
    // only 'mu' entries.
    import spark.implicits._
    val neighbors = distanceMatrix.groupBy("i_num").agg(topN(mu)(pairwisePairUp(collect_list($"j_num"), collect_list($"distance"))).as("arr"))
    neighbors
  }

  private def getPBetaValuesDF(neighbors: DataFrame, tol: Double, perplexity: Double)(implicit spark: SparkSession): RDD[(mutable.WrappedArray[MatrixEntry], Double)] = {
    val logU = Math.log(perplexity)

    neighbors.rdd.map {
      case (row: Row) =>
        val i = row.getAs[Long]("i_num")
        val arr = row.getAs[mutable.WrappedArray[GenericRowWithSchema]]("arr")

        var betamin = Double.NegativeInfinity
        var betamax = Double.PositiveInfinity
        var beta = 1.0

        val values = arr.map{ r: GenericRowWithSchema => r.getAs[Double](1) }.toArray
        val d = DenseVector(values)
        var (h, p) = Hbeta(d, beta)

        //logInfo("data was " + d.toArray.toList)
        //logInfo("array P was " + p.toList)

        // Evaluate whether the perplexity is within tolerance
        def Hdiff = h - logU

        var tries = 0
        while (Math.abs(Hdiff) > tol && tries < 50) {
          //If not, increase or decrease precision
          if (Hdiff > 0) {
            betamin = beta
            beta = if (betamax.isInfinite) {
              beta * 2
            }
            else {
              (beta + betamax) / 2
            }
          }
          else {
            betamax = beta
            beta = if (betamin.isInfinite) {
              beta / 2
            }
            else {
              (beta + betamin) / 2
            }
          }

          // Recompute the values
          val HP = Hbeta(d, beta)
          h = HP._1
          p = HP._2
          tries = tries + 1
        }

        //logInfo("array P is " + p.toList)

        (arr.map{ r => r.getAs[Long](0) }.zip(p.toArray).map { case (j, v) => MatrixEntry(i, j, v) }, beta)
    }
  }

  private def getPBetaValues(neighbors: RDD[(Long, Array[(Long, Double)])], tol: Double, perplexity: Double) = {
    val logU = Math.log(perplexity)

    neighbors.map {
      case (i, arr) =>
        var betamin = Double.NegativeInfinity
        var betamax = Double.PositiveInfinity
        var beta = 1.0

        val values = arr.map(_._2).toArray
        val d = DenseVector(values)
        var (h, p) = Hbeta(d, beta)

        //logInfo("data was " + d.toArray.toList)
        //logInfo("array P was " + p.toList)

        // Evaluate whether the perplexity is within tolerance
        def Hdiff = h - logU

        var tries = 0
        while (Math.abs(Hdiff) > tol && tries < 50) {
          //If not, increase or decrease precision
          if (Hdiff > 0) {
            betamin = beta
            beta = if (betamax.isInfinite) {
              beta * 2
            }
            else {
              (beta + betamax) / 2
            }
          }
          else {
            betamax = beta
            beta = if (betamin.isInfinite) {
              beta / 2
            }
            else {
              (beta + betamin) / 2
            }
          }

          // Recompute the values
          val HP = Hbeta(d, beta)
          h = HP._1
          p = HP._2
          tries = tries + 1
        }

        //logInfo("array P is " + p.toList)

        (arr.map(_._1).zip(p.toArray).map { case (j, v) => MatrixEntry(i, j, v) }, beta)
    }
  }

  def apply(x: DataFrame, tol: Double, perplexity: Double)(implicit spark: SparkSession): CoordinateMatrix = {
    require(tol >= 0, "Tolerance must be non-negative")
    require(perplexity > 0, "Perplexity must be positive")
    val mu = (3 * perplexity).toInt //TODO: Expose this as parameter

    val distanceMatrix: DataFrame = toDistanceMatrix(x)(spark)

    distanceMatrix.printSchema()
    val neighbors = computeRankedNeighbors(mu, distanceMatrix)
    neighbors.collect.foreach{
      case row: Row  =>
        val i = row.getAs[Long](0)
        val arr = row.getAs[mutable.WrappedArray[(Long, Double)]](1)
        print(s"${i}\t")
        print(arr.mkString(" "))
        println()
    }
    val p_betas =
      getPBetaValuesDF(neighbors, tol, perplexity)

    logger.info("Mean value of sigma: " + p_betas.map(x => math.sqrt(1 / x._2)).mean)
    new CoordinateMatrix(p_betas.flatMap(_._1))
  }

  def apply(x: RowMatrix, tol: Double = 1e-5, perplexity: Double = 30.0)(sparkContext: SparkContext): CoordinateMatrix = {
    require(tol >= 0, "Tolerance must be non-negative")
    require(perplexity > 0, "Perplexity must be positive")

    val mu = (3 * perplexity).toInt //TODO: Expose this as parameter
    val norms = x.rows.map(Vectors.norm(_, 2.0))
    norms.persist()

    // The idea for the following was gotten from
    // http://stackoverflow.com/questions/38828139/spark-cartesian-product/38831748#38831748.
    // Someone had the same problem that the flatmap after the cartesian product took too much.
    val rowsWithNorm = x.rows.zip(norms).map{ case (v, norm) => VectorWithNorm(v, norm) }

    val neighbors: RDD[(Long, Array[(Long, Double)])] = rowsWithNorm.zipWithIndex()
      .cartesian(rowsWithNorm.zipWithIndex())
      .flatMap {
      case ((u, i), (v, j)) =>
        if(i < j) {
          val dist = fastSquaredDistance(u, v)
          Seq((i, (j, dist)), (j, (i, dist)))
        } else Seq.empty
    }
      .topByKey(mu)(Ordering.by(e => -e._2))

    neighbors.collect.foreach{
      case (i: Long, arr: Array[(Long, Double)]) =>
        print(s"${i}\t")
        print(arr.mkString(" "))
        println()
    }

    val p_betas =
      getPBetaValues(neighbors, tol, perplexity)

    logger.info("Mean value of sigma: " + p_betas.map(x => math.sqrt(1 / x._2)).mean)
    new CoordinateMatrix(p_betas.flatMap(_._1))
  }
}
