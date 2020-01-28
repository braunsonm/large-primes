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
      println("Usage: [calc directory] [n]")
      sys.exit(1)
    }

    val dir = new URI(args(0))
    val numBits = args(1).toInt

    // Force lazy evaluation of the spark context if not already created
    val sc = Spark.sc
    val program = s"${dir.getPath}${Path.SEPARATOR}calc"
    val primeFiles = mutable.ListBuffer[File]()

    for (i <- 0 until 14) {
      primeFiles.append(new File(dir.getPath + Path.SEPARATOR + s"prime${i.toString}.txt"))
    }

    var possiblePrimes = mutable.ListBuffer[String]()
    var rand = BigInt(numBits, scala.util.Random)
    val startTime = System.nanoTime()

    var continue = true
    while(continue) {
      primeFiles.foreach(file => {
        rand = BigInt(numBits, scala.util.Random)
        rand = rand.setBit(0) // 2^10000 - 2^10001
        if (rand % 2 == 0) rand += 1

        val out = new FileWriter(file, false)
        out.write(rand.toString)
        out.close()
      })

      val nums = sc.parallelize(List.tabulate(Spark.defaultParallelism)(index => s"${dir.getPath}${Path.SEPARATOR}prime${index % 14}.txt"))
      val result = nums.pipe(program).collect()
      val isPrime = mutable.Map[String, Boolean]()
      primeFiles.foreach(file => isPrime(file.getAbsolutePath) = true)
      var i = 0
      while (isPrime.values.exists(bool => bool) && i < result.length) {
        if (result(i).split(' ').last.contains("0")) {
          isPrime(result(i).split(' ').head) = false
        }
        i += 1
      }

      if (isPrime.values.exists(bool => bool)) {
        // Run 50 checks on any that passed
        logger.info("Possible prime found: " + isPrime.filter(tuple => tuple._2).keys.mkString(" "))
        possiblePrimes = mutable.ListBuffer[String]()
        isPrime.filter(tuple => tuple._2).foreach(tuple => {
          val nums = sc.parallelize(List.tabulate(Spark.defaultParallelism)(_ => tuple._1))
          val result = nums.pipe(program).collect()
          var passed = true
          var ii = 0
          while (passed && ii < result.length) {
            if (result(ii).split(' ').last.contains("0")) {
              passed = false
            }
            ii += 1
          }

          if (passed) possiblePrimes.append(tuple._1)
        })

        if (possiblePrimes.nonEmpty) continue = false
      }
    }

    val endTime = System.nanoTime()

    logger.info(s"Prime prob complete. Time elapsed = ${(endTime - startTime) / 1e9}s")
    possiblePrimes.foreach(logger.info)
  }
}
