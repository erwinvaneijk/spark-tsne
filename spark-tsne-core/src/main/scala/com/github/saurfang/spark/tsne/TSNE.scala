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
  * Created by kojak on 05/02/17.
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
