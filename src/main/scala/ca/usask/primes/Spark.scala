package ca.usask.primes

import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}

object Spark {

  val BLOCK_SIZE: Long = 128 * 1024 * 1024

  val APP_NAME = "PRIMES"
  val MASTER = "local[*]"
  val DISABLE_SPARK_LOGS = true
  val ES_HOST = "localhost"

  private lazy val logger = java.util.logging.Logger.getLogger(APP_NAME)

  lazy val sc: SparkContext = {
    logger
      .info(
        s"\n" +
          s"${"*" * 80}\n" +
          s"${"*" * 80}\n" +
          s"**${" " * 76}**\n" +
          s"**${" " * (38 - Math.ceil(APP_NAME.length / 2.0).toInt)}$APP_NAME${" " * (38 - APP_NAME.length / 2)}**\n" +
          s"**${" " * 76}**\n" +
          s"${"*" * 80}\n" +
          s"${"*" * 80}\n"
      )

    if (DISABLE_SPARK_LOGS) {
      Logger.getLogger("org").setLevel(Level.OFF)
      Logger.getLogger("akka").setLevel(Level.OFF)
    }

    val conf = new SparkConf()
    conf.set("spark.ui.showConsoleProgress", "true")
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")

    if (!conf.contains("spark.app.name")) {
      conf.setAppName(APP_NAME)
    }

    if (!conf.contains("spark.master")) {
      conf.setMaster(MASTER)
    }

    val ctx = new SparkContext(conf)

    logger.info(s"Master is ${ctx.master}")
    logger.info(s"Default parallelism = ${ctx.defaultParallelism}")

    ctx
  }

  def defaultParallelism: Int = sc.defaultParallelism
}
