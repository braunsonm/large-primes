package ca.usask.primes

import java.io.{File, FileWriter}
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.logging.Logger

import org.apache.hadoop
import org.apache.hadoop.fs.Path

import scala.sys.process._

object primes {
  def main(args: Array[String]): Unit = {
    val logger = Logger.getLogger("P2IRC-filter-benchmark")
    if (args.length != 2) {
      println("Usage: [calc directory] [n]")
      sys.exit(1)
    }

    val dir = new URI(args(0))
    val numBits = args(1).toInt

    // Force lazy evaluation of the spark context if not already created
    val sc = Spark.sc
    val program = s"${dir.getPath}${Path.SEPARATOR}calc"
    sc.addFile(program)

    val p = new File(dir.getPath + Path.SEPARATOR + "prime.txt")
    var rand = BigInt(numBits, scala.util.Random)
    val startTime = System.nanoTime()

    var continue = true
    while(continue) {
      rand = BigInt(numBits, scala.util.Random)
      rand = rand.setBit(0) // 2^10000 - 2^10001

      val out = new FileWriter(p, false)
      out.write(rand.toString)
      out.close()

      val nums = sc.parallelize(List.tabulate(Spark.defaultParallelism)(_ => ""))
      val result = nums.pipe(Seq(s".${Path.SEPARATOR}calc ${dir.getPath}${Path.SEPARATOR}prime.txt")).collect()
      var isPrime = true
      var i = 0
      while (isPrime || i < result.length) {
        if (result(i).contains("0")) {
          isPrime = false
        }
        i += 1
      }

      if (isPrime) continue = false
    }

    val endTime = System.nanoTime()

    logger.info(s"Prime prob complete. Time elapsed = ${(endTime - startTime) / 1e9}s")
    logger.info(s"${rand}")
  }
}
