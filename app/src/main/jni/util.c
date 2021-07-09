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
#include "util.h"
#include "stdio.h"
#include "string.h"

int hex_print(char* title, unsigned char* input, int size)
{
    int i = 0;
    char converted[size*2 + 1];

    while (i<size)
    {
        sprintf(&converted[i*2], "%02X", input[i]);
        i++;
    }
#ifdef CLIENT_SIDE
    __android_log_print(ANDROID_LOG_DEBUG, ANDROID_LOG_TAG, "[%s] %d", title, size);
#else
    printf("[%s] %d ", title, size);
#endif
#ifdef DETAIL_DEBUG
    printf("%s\n", converted);
#endif

    return 0;
}

void eliminate_str(char* target, char eliminator)
{
    int i=0, j=0;
    while (j < strlen(target)) {
        if (target[j] == eliminator) {}
        else {
            target[i] = target[j];
            i++;
        }
        j++;
    }
    target[i] = '\0';
}
