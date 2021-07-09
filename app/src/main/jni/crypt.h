/*
 * Copyright (c) 2021 HopeBayTech.
 *
 * This file is part of Tera.
 * See https://github.com/HopeBayMobile for further info.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
