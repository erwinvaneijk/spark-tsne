package com.github.saurfang.spark.tsne.ml

import javax.xml.transform.Transformer

import org.apache.spark.ml.param._
import com.github.saurfang.spark.tsne.ml._

private[ml] trait TSNEParams extends Params with HasInputCol with HasOutputCol

/**
  * The pipeline component for using tSNE.
  *
  * Created by kojak on 11/11/16.
  */
class TSNE(override val uid: String) extends Transformer{

}
