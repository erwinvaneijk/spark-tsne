package org.apache.spark

import org.apache.spark.sql.SparkSession
import org.scalatest.{BeforeAndAfterAll, Suite}

/** Shares a local `SparkContext` between all tests in a suite and closes it at the end */
trait SharedSparkContext extends BeforeAndAfterAll { self: Suite =>

  @transient private var _sparkSession: SparkSession = _

  def sc: SparkContext = _sparkSession.sparkContext

  var conf = new SparkConf(false)

  override def beforeAll() {
    _sparkSession = SparkSession.builder().master("local[4]").appName("tsne-test").config(conf).getOrCreate()
    super.beforeAll()
  }

  override def afterAll() {
    _sparkSession.stop()
    _sparkSession = null
    super.afterAll()
  }
}
