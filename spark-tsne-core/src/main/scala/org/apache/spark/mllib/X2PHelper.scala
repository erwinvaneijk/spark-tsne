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

package org.apache.spark.mllib

import breeze.linalg._
import breeze.numerics._
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.util.MLUtils


object X2PHelper {

  case class VectorWithNorm(vector: Vector, norm: Double)

  def fastSquaredDistance(v1: VectorWithNorm, v2: VectorWithNorm): Double = {
    MLUtils.fastSquaredDistance(v1.vector, v1.norm, v2.vector, v2.norm)
  }

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
