#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <errno.h>
#include <time.h>

// GNU Multiple Precision Arithmetic Library
#include <gmp.h>
// For implementation, see mpz/millerrabin.c

// Fast alternative to modulo reduction
// https://lemire.me/blog/2016/06/27/a-fast-alternative-to-the-modulo-reduction/

// https://stackoverflow.com/questions/50285210/generating-random-values-without-time-h
static int randomize_helper(FILE *in)
{
    unsigned int  seed;

    if (!in)
        return -1;

    if (fread(&seed, sizeof seed, 1, in) == 1) {
        fclose(in);
        srand(seed);
        return 0;
    }

    fclose(in);
    return -1;
}

static int randomize(void)
{
    if (!randomize_helper(fopen("/dev/urandom", "r")))
        return 0;
    if (!randomize_helper(fopen("/dev/arandom", "r")))
        return 0;
    if (!randomize_helper(fopen("/dev/random", "r")))
        return 0;

    /* No randomness sources found. */
    return -1;
}

int main() {
    mpz_t n; mpz_init(n);
    mpz_t randmpz; mpz_init(randmpz);
    mpz_t one; mpz_init(one);
    int is_prime = 0;
    randomize();
    unsigned long seed = rand();
    unsigned long startTime = time(NULL);
    gmp_randstate_t randstate;

    // printf("%s opened\n", fileName);
    mpz_set_ui(one, 1);
    mpz_inp_str(n, NULL, 10);
    //gmp_printf("%Zd\n", n);

    gmp_randinit_default(randstate);
    gmp_randseed_ui(randstate, seed);

    // Run for an hour or until a prime is found
    while(time(NULL) - startTime <= 3600 && is_prime != 1) {
        mpz_urandomm(randmpz, randstate, n);
        mpz_add(randmpz, randmpz, n);
        if (mpz_even_p(randmpz) != 0)
            mpz_add(randmpz, randmpz, one);

        // 0=not prime, 1=probably prime
        is_prime = mpz_probab_prime_p(randmpz, 25);
        if (is_prime == 2)
            // For sure a prime
            is_prime = 1;
    }
    
    if (is_prime == 1)
        gmp_printf("%d %Zd\n", is_prime, randmpz);
    else
        gmp_printf("%d 0\n", is_prime); // Don't waste bandwidth
    return 0;
}
