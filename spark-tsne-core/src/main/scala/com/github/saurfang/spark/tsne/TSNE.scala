/*
 * Copyright (c) 2017, Erwin van EIjk
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

import org.apache.spark.ml.Transformer
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.ml.param.{Params, _}
import org.apache.spark.ml.util.{DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.functions.udf
import org.apache.spark.sql.types.{ArrayType, StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Dataset}

private[tsne] trait TSNEParams extends Params with HasInputCol with HasOutputCol with DefaultParamsWritable {
}

/**
  * Implements the Transformer interface, in order to include TSNE in a
  * generic ml-pipeline.
  */
class TSNE(override val uid: String) extends Transformer with TSNEParams {
  def this() = this(Identifiable.randomUID("gather"))

  def setOutputCol(value: String): this.type = set(outputCol, value)

  def setInputCol(value: String): this.type = set(inputCol, value)

  override def transformSchema(schema: StructType): StructType = {
    schema.add(StructField($(outputCol), ArrayType(StringType), nullable = false))
  }

  override def transform(dataset: Dataset[_]): DataFrame = {
    val outputSchema = transformSchema(dataset.schema)
    val metadata = outputSchema($(outputCol)).metadata

    val cleanConversion = udf{ vec :  Vector =>
    }
    dataset.withColumn($(outputCol), cleanConversion(dataset.col($(inputCol))))//.as($(outputCol), metadata))
  }

  override def copy(extra: ParamMap): Transformer = defaultCopy(extra)

}
