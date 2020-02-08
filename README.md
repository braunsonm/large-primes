# Large Primes on Spark

This is a simple Spark application that uses the [GNU Multiple Precision Library (GMP)](https://gmplib.org/) to find and verify large primes.

This was done as a fun contest in my Cryptography class. Sorry for the messy code,
most of this was done in one night to see how far I could get.

## Requirements

- Spark (2.4.1+)
- Some form of shared file system (Hadoop, NFS share, etc)
- Scala 2.12.8+
- GMP 6.2.0+

## Running

Copy the `.env.template` to `.env`.  Edit the required environment variables with
options relevant to your setup. 

- `HDFS_DIRECTORY` is the shared file system location.

### Finding a Prime

The `primes` Spark application will search for primes between N and 2N defined by
the environment variable `NUM_BITS`. 

The algorithm is as follows:

- Each executor core will be given the prime to compute on standard in
- The core will then generate some randomness using the linux random outputs,
we do not need cryptographically secure entropy.
- The core will then run for an hour, or until a prime is found by selecting
random numbers (between N and 2N) and calling GMP's probable prime function.
    - Before running Ballie-PSW, we check our number against the first B primes
    where B is the number of bits needed to represent our random number in base 2
    - The GMP probably prime function runs Ballie-PSW, with r-24 reps of Miller
    Rabin. We trigger to only run 1 miller rabin round after Ballie-PSW. Then
    we report this "probable prime" to the driver so we can run the verify program.
    - We do this as an optimization since it's much harder to find a candidate number
    than it is to calculate the Ballie-PSW. This way we can try as many numbers as
    possible before wasting time on Miller-Rabin.
    - We check to ensure our number is odd before beginning our call to GMP
- Spark collects all the standard output from each application after they close (as
long as it's a zero exit code) and checks for any executors that report a 
probable prime. If one is found, end the application
- Otherwise we repeat. Creating a new Spark stage.

Run the automated script:
`scripts/submit.sh`

### Verify a Found Prime

The `verify` Spark application will run a single round of Miller Rabin on each core. 
In my specific setup, that meant 72 rounds of Miller Rabin. Note that the code
is triggered with 25 rounds, but this is because Ballie-PSW runs and subtracts 
the number of rounds by 24 before running Miller Rabin. 

The prime you are testing for will be looked for in `HDFS_DIRECTORY/foundprime.txt`,
so just paste your prime in a file named that.

Run the automated script:
`scripts/verify.sh`

## Challenges along the way

Some challenges I encountered during this contest:

- Finding large random numbers all on the master node was way too slow in Scala,
instead offloading that job to each executor to calculate on their own was much better.
- Calculating the a's of Miller-Rabin is generally very fast until you get to really
high numbers. The main delay is in finding a good candidate random number as they
become more and more sparse the higher in magnitude you get. That's why each core
runs through it's own random number.

## Improvements to be made

Some future improvements that could be completed include,

- The verify program doesn't need to run Ballie-PSW really, it just needs to run
Miller-Rabin since the number already passed Ballie-PSW.
- We can use a Sieve similar to the "nextprime" function does in GMP. Then, each
executor core could be given a range of the sieve to run through looking for primes.
This would give us better than "random" selection of numbers.
- Caching of the a^k values of Miller-Rabin if we use the same a values for each core
running Miller-Rabin would greatly speed up that computation (dynamic programming approach may also be investigated with Ballie-PSW)
 