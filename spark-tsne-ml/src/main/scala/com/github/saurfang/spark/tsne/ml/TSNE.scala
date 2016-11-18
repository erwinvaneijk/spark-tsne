package com.github.saurfang.spark.tsne.ml

import org.apache.spark.annotation.DeveloperApi
import org.apache.spark.ml.Transformer
import org.apache.spark.ml.param._
import org.apache.spark.sql.{DataFrame, Dataset}
import org.apache.spark.sql.types.StructType

private[ml] trait TSNEParams extends Params with HasInputCol with HasOutputCol

/**
  * The pipeline component for using tSNE.
  *
  * Created by kojak on 11/11/16.
  */
class TSNE(override val uid: String) extends Transformer {

  override def transform(dataset: Dataset[_]): DataFrame = ???

  override def copy(extra: ParamMap): Transformer = ???

  @DeveloperApi
  override def transformSchema(
    schema: StructType): StructType = ???
}
