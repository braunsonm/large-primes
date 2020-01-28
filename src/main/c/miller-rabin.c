#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

// GNU Multiple Precision Arithmetic Library
#include <gmp.h>
// For implementation, see mpz/millerrabin.c

// Fast alternative to modulo reduction
// https://lemire.me/blog/2016/06/27/a-fast-alternative-to-the-modulo-reduction/

int main(int argc, char** argv) {
    FILE* file;
    mpz_t n; mpz_init(n);
    int is_prime;

    if (argc != 2) {
        printf("Usage: [filename]\n");
        return 1;
    }

    //printf("Opening %s\n", argv[1]);
    file = fopen(argv[1], "r");

    mpz_inp_str(n, file, 10);
    //gmp_printf("%Zd\n", n);

    // 0=not prime, 1=probably prime
    is_prime = mpz_millerrabin(n, 1);
    printf("%d\n", is_prime);
    return is_prime;
}
