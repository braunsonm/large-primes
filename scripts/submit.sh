#!/bin/bash

# go one level up from script location (presumably project root) and load .env
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd $DIR/..
source .env

export JAVA_HOME=$JAVA_HOME

sbt assembly
make -C src/main/c

cp src/main/c/calc $HDFS_DIRECTORY/calc

$SPARK_ROOT/bin/spark-submit \
  --master $SPARK_MASTER \
  --conf spark.app.name=P2IRC-ingest \
  --conf spark.driver.cores=$DRIVER_CORES \
  --conf spark.driver.memory=$DRIVER_MEMORY \
  --conf spark.executor.instances=$NUM_EXECUTORS \
  --conf spark.default.parallelism=$TOTAL_EXECUTOR_CORES \
  --conf spark.executor.cores=$CORES_PER_EXECUTOR \
  --conf spark.executor.memory=$MEMORY_PER_EXECUTOR \
  --conf spark.memory.fraction=$MEMORY_FRACTION \
  --class ca.usask.primes.primes \
  $LOCAL_PROJECT_ROOT/$JAR \
  $HDFS_DIRECTORY $NUM_BITS