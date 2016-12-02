package org.apache.spark.ml

import breeze.linalg._
import breeze.numerics._
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.mllib.util.MLUtils

object X2PHelper {

  case class VectorWithNorm(vector: Vector, norm: Double)

  def Hbeta(D: DenseVector[Double], beta: Double = 1.0) : (Double, DenseVector[Double]) = {
    val P: DenseVector[Double] = exp(- D * beta)
    val sumP = sum(P)
    if(sumP == 0) {
      (0.0, DenseVector.zeros(D.size))
    }else {
      val H = log(sumP) + (beta * sum(D :* P) / sumP)
      (H, P / sumP)
    }
  }
}
