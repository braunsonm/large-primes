package ca.usask.primes

import java.io.{File}
import java.net.URI
import java.util.logging.Logger

import org.apache.hadoop.fs.Path

import scala.collection.mutable

object verifyPrimes {
  def main(args: Array[String]): Unit = {
    val logger = Logger.getLogger("Prime-Verifier")
    if (args.length != 2) {
      println("Usage: [calc directory] [n]")
      sys.exit(1)
    }

    val dir = new URI(args(0))

    // Force lazy evaluation of the spark context if not already created
    val sc = Spark.sc
    val program = s"${dir.getPath}${Path.SEPARATOR}verify"
    val primeFiles = mutable.ListBuffer[File]()

    val startTime = System.nanoTime()
    var bad = false
    val nums = sc.parallelize(List.tabulate(Spark.defaultParallelism)(_ => s"${dir.getPath}${Path.SEPARATOR}primefound.txt"))
    val result = nums.pipe(program).collect()
    val isPrime = mutable.Map[String, Boolean]()
    primeFiles.foreach(file => isPrime(file.getAbsolutePath) = true)
    var i = 0
    while (isPrime.values.exists(bool => bool) && i < result.length) {
      if (result(i).split(' ').last.contains("0")) {
        bad = true
        i = result.length
      }
      i += 1
    }

    val endTime = System.nanoTime()

    logger.info(s"Prime prob complete. Real Prime?: ${!bad} Time elapsed = ${(endTime - startTime) / 1e9}s")
  }
}
