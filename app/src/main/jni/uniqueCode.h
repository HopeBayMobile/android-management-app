#include <openssl/pem.h>
#include <openssl/ssl.h>
#include <openssl/rsa.h>
#include <openssl/evp.h>
#include <openssl/bio.h>
#include <openssl/err.h>
#include <string.h>
#include <stdio.h>
#include "base64.h"

int getUniqueCode(unsigned char*);
int getEncryptCode(unsigned char* encrypt_code, size_t* output_length);
int encryptCode(unsigned char* encrypt_code, const unsigned char* plain_text, const size_t* input_length);
