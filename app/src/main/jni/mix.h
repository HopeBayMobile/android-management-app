#include "enc.h"
#include "crypt.h"
#include "params.h"
#ifdef CLIENT_SIDE
#include <android/log.h>
#endif

#define ENCRYPTED_KEY_SIZE 345

#ifndef CLIENT_SIDE
// output = base64 encode (random key + encrypted input + tag size)
int teraPrivateEncrypt(unsigned char* output, unsigned char* input);
#endif
int teraPublicDecrypt(unsigned char* output, unsigned char* input);

int hex_print(char* title, unsigned char* input, int size);
