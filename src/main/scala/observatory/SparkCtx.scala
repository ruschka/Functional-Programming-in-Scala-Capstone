package observatory

import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author Vojtech Ruschka
  */
object SparkCtx {

  Logger.getLogger("org.apache.spark").setLevel(Level.WARN)

  lazy val conf: SparkConf = new SparkConf().setMaster("local").setAppName("observatory")
  lazy val ctx: SparkContext = new SparkContext(conf)

}
