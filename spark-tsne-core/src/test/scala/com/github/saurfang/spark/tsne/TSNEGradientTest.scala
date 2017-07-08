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

package com.github.saurfang.spark.tsne

import breeze.linalg._
import org.scalatest.{FunSuite, Matchers}

/**
 * Created by forest on 7/17/15.
 */
class TSNEGradientTest extends FunSuite with Matchers {
  test("computeNumerator should compute numerator for sub indices") {
    val Y = DenseMatrix.create(3, 2, (1 to 6).map(_.toDouble).toArray)
    println(Y)
    val num = TSNEGradient.computeNumerator(Y, 0, 2)
    println(num)
  }
}
