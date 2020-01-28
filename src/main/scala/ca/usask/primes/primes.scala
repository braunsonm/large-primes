package ca.usask.primes

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

    def fs = {
      val conf = new hadoop.conf.Configuration()
      conf.set("fs.default.name", s"hdfs://${dir.getHost}:${dir.getPort}")
      hadoop.fs.FileSystem.get(conf)
    }

    val p = new Path(dir.getPath + Path.SEPARATOR + "prime.txt")
    var rand = BigInt(numBits, scala.util.Random)
    val startTime = System.nanoTime()

    var continue = true
    while(continue) {
      rand = BigInt(numBits, scala.util.Random)
      rand = rand.setBit(0) // 2^10000 - 2^10001

      val out = fs.create(p)
      out.write(rand.toString.getBytes(StandardCharsets.US_ASCII))
      out.close()

      val nums = sc.parallelize(List.tabulate(Spark.defaultParallelism)(_ => ""))
      nums.map(_ => {
        fs.copyToLocalFile(false, new Path(dir.getPath + Path.SEPARATOR + "prime.txt"),
          new Path("file://" + Path.SEPARATOR + "tmp" + hadoop.fs.Path.SEPARATOR + "prime.txt"), true)
        fs.copyToLocalFile(false, new Path(dir.getPath + Path.SEPARATOR + "calc"),
          new Path("file://" + Path.SEPARATOR + "tmp" + Path.SEPARATOR + "calc"), true)
      }).count()

      val result = nums.pipe(Seq("/tmp/calc /tmp/prime.txt")).collect()
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
