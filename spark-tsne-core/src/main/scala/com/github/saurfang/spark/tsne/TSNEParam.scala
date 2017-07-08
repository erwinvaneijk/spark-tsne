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

case class TSNEParam(
                      early_exaggeration: Int = 100,
                      exaggeration_factor: Double = 4.0,
                      t_momentum: Int = 25,
                      initial_momentum: Double = 0.5,
                      final_momentum: Double = 0.8,
                      eta: Double = 500.0,
                      min_gain: Double = 0.01
                      )
