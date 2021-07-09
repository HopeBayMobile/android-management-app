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
#include "enc.h"
#include "util.h"
#include "crypt.h"
#include "params.h"

#define ENCRYPTED_KEY_SIZE 345

#ifndef CLIENT_SIDE
// output = base64 encode (random key + encrypted input + tag size)
int teraPrivateEncrypt(unsigned char* output, unsigned char* input);
#endif
int teraPublicDecrypt(unsigned char* output, unsigned char* input);
