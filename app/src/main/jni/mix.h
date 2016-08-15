#include "crypt.h"
#include "enc.h"

#define ENCRYPTED_KEY_SIZE 344

int teraPublicDecrypt(unsigned char* output, unsigned char* input);

int hex_print(unsigned char* input, int size);
