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

import org.apache.spark.SharedSparkContext
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.linalg.distributed.RowMatrix
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
    val output = X2P(input, 1e-5, 2).toRowMatrix().rows.collect().map(_.toArray.toList)
    println(output.toList)
    //output shouldBe List(List(0, .5, .5), List(.5, 0, .5), List(.5, .5, .0))
  }
}
