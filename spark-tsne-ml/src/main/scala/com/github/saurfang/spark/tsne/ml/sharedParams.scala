package nl.oakhill.datascience.raop

import org.apache.spark.ml.param.{Param, Params}

/**
  * Trait for shared param keyCol.
  */
private[raop] trait HasKeyCol extends Params {

  /**
    * Param for category column name.
    * @group param
    */
  final val keyCol: Param[String] = new Param[String](this, "keyCol",
    "Column that holds value for category name")

  /** @group getParam */
  def getCategoryCol: String = $(keyCol)
}


/**
  * Trait for shared param outputCol (default: uid + "__output").
  */
private[raop] trait HasOutputCol extends Params {

  /**
    * Param for output column name.
    * @group param
    */
  final val outputCol: Param[String] = new Param[String](this, "outputCol", "output column name")

  setDefault(outputCol, uid + "__output")

  /** @group getParam */
  final def getOutputCol: String = $(outputCol)
}

/**
  * Trait for shared param inputCol (default: uid + "__input").
  */
private[raop] trait HasInputCol extends Params {

  /**
    * Param for input column name.
    * @group param
    */
  final val inputCol: Param[String] = new Param[String](this, "inputCol", "input column name")

  setDefault(inputCol, uid + "__input")

  /** @group getParam */
  final def getInputCol: String = $(inputCol)
}
