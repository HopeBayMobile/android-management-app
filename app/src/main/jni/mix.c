#include "mix.h"

// #define DEBUG

/*
 * @param unsigned char* input: Encrypted text going to encrypt.
 * @param unsigned char* output: input * 3 / 4 - KEY_SIZE - TAG_SIZE + 1
 */
int teraPublicDecrypt(unsigned char* output, unsigned char* input)
{
    int decoded_length = 0;
    int input_length = strlen(input);

    unsigned char decoded[input_length];

    // Base64 decode
    if (b64decode_str(input, decoded, &decoded_length, input_length) != 0)
        return -1;

    decoded_length++;
    int encrypted_length = decoded_length - ENCRYPTED_KEY_SIZE;
    unsigned char key[KEY_SIZE];
    unsigned char encrypted[encrypted_length];
    unsigned char encrypted_key[ENCRYPTED_KEY_SIZE];
    // Seperate key and encrypted from decoded text
    memset(key, 0, KEY_SIZE);
    memset(encrypted, 0, encrypted_length);
    memset(encrypted_key, 0, ENCRYPTED_KEY_SIZE);

    memcpy(encrypted_key, decoded, ENCRYPTED_KEY_SIZE);
    memcpy(encrypted, decoded+ENCRYPTED_KEY_SIZE, encrypted_length);

    // Decrypt AES256 key by RSA
    if (publicDecryptCode(key, encrypted_key, ENCRYPTED_KEY_SIZE) != 0)
        return -1;

#ifdef DEBUG
    printf("\n");
    printf("[DEC key]"); hex_print(key, sizeof(key));
    printf("[DEC encrypted key]"); hex_print(encrypted_key, sizeof(encrypted_key));
    printf("[DEC encrypted]"); hex_print(encrypted, sizeof(encrypted));
#endif
    // Start to decrypt
    if (aes_gcm_decrypt_fix_iv(output, encrypted, sizeof(encrypted), key) != 0) {
        printf("error\n");
        return -1;
    }
    output[encrypted_length - TAG_SIZE] = '\0';

    return 0;
}

int hex_print(unsigned char* input, int size) {
    int i = 0;
    char converted[size*2 + 1];

    while (i<size)
    {
        sprintf(&converted[i*2], "%02X", input[i]);
        i++;
    }
    printf("%lu %s\n", size, converted);

    return 0;
}
