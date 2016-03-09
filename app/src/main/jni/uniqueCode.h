//
// Created by Aaron on 2016/3/8.
//

#include <openssl/pem.h>
#include <openssl/ssl.h>
#include <openssl/rsa.h>
#include <openssl/evp.h>
#include <openssl/bio.h>
#include <openssl/err.h>
#include <stdio.h>

int getUniqueCode(unsigned char*);
int getEncryptCode(unsigned char* encrypt_code);