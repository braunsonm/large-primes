package ca.usask.primes

import java.io.{File, FileWriter}
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.logging.Logger

import org.apache.hadoop
import org.apache.hadoop.fs.Path

import scala.collection.mutable
import scala.sys.process._

object primes {
  def main(args: Array[String]): Unit = {
    val logger = Logger.getLogger("Prime-Finder")
    if (args.length != 2) {
      println("Usage: [calc directory] [2^n]")
      sys.exit(1)
    }

    val dir = new URI(args(0))
    val num = BigInt(2).pow(args(1).toInt)

    // Force lazy evaluation of the spark context if not already created
    val sc = Spark.sc
    val program = s"${dir.getPath}${Path.SEPARATOR}calc"
    val possiblePrimes = mutable.ListBuffer[String]()
    val startTime = System.nanoTime()

    var continue = true
    while(continue) {
      val nums = sc.parallelize(List.tabulate(Spark.defaultParallelism)(_ => num))
      val result = nums.pipe(program).collect()
      var i = 0
      while (i < result.length) {
        if (result(i).split(' ').head.contains("1")) {
          possiblePrimes.append(result(i).split(' ').last)
        }
        i += 1
      }

      if (possiblePrimes.nonEmpty) {
        continue = false
      }
    }

    val endTime = System.nanoTime()

    logger.info(s"Prime prob complete. Time elapsed = ${(endTime - startTime) / 1e9}s")
    possiblePrimes.toSet[String].foreach(logger.info)

    val out = new FileWriter(new File(s"${dir.getPath}${Path.SEPARATOR}primes.txt"), false)
    out.write(possiblePrimes.toSet[String].mkString("\n\n"))
    out.close()
  }
}
