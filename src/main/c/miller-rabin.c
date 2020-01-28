#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>

// GNU Multiple Precision Arithmetic Library
#include <gmp.h>
// For implementation, see mpz/millerrabin.c

// Fast alternative to modulo reduction
// https://lemire.me/blog/2016/06/27/a-fast-alternative-to-the-modulo-reduction/

int readInput(char *buffer, int buffer_size) {
    /* FGet input from the user */
    memset(buffer, 0, buffer_size);
    fgets(buffer, buffer_size, stdin);
    buffer[strlen(buffer) - 1] = '\0'; // clip off newline char

    return 0;
}

int main() {
    FILE* file;
    mpz_t n; mpz_init(n);
    int is_prime;
    char fileName[1024];

    readInput(fileName, 1024);

    printf("%s opened\n", fileName);
    file = fopen(fileName, "r");
    if (!file) {
        printf("Failed to open 0\n");
        return 1;
    }

    mpz_inp_str(n, file, 10);
    //gmp_printf("%Zd\n", n);

    // 0=not prime, 1=probably prime
    is_prime = mpz_millerrabin(n, 1);
    printf("%s %d\n", fileName, is_prime);
    return 0;
}
