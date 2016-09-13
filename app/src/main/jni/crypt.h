#include <openssl/pem.h>
#include <openssl/ssl.h>
#include <openssl/rsa.h>
#include <openssl/evp.h>
#include <openssl/bio.h>
#include <openssl/err.h>
#include <string.h>
#include <stdio.h>
#include "params.h"

#ifdef CLIENT_SIDE
#include <sys/system_properties.h>

int getUniqueCode(unsigned char*);
int getEncryptCode(unsigned char* encrypt_code, size_t* output_length);
#endif

int publicEncryptCode(unsigned char* encrypt_code, const unsigned char* plain_text, const size_t* input_length);
int publicDecryptCode(unsigned char* decrypt_code, const unsigned char* encrypt_code, const size_t output_length);
#ifndef CLIENT_SIDE
int privateEncryptCode(unsigned char* encrypt_code, const unsigned char* plain_text, const size_t* input_length);
int privateDecryptCode(unsigned char* decrypt_code, const unsigned char* encrypt_code, const size_t output_length);
#endif
