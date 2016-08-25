#include "enc.h"
#include "crypt.h"
#include "params.h"

#define ENCRYPTED_KEY_SIZE 345

#ifndef CLIENT_SIDE
// output = base64 encode (random key + encrypted input + tag size)
int teraPrivateEncrypt(unsigned char* output, unsigned char* input);
#endif
int teraPublicDecrypt(unsigned char* output, unsigned char* input);

int hex_print(unsigned char* input, int size);
