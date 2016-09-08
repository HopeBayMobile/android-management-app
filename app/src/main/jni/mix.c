#include "mix.h"


#ifndef CLIENT_SIDE
/*
 * @param unsigned char* input: plain text going to encrypt.
 * @param unsigned char* output: expect_b64_encode_length( input + TAG_SIZE + ENCRYPTED_KEY_SIZE)
 */
int teraPrivateEncrypt(unsigned char* output, unsigned char* input)
{
    int input_length = strlen(input);
    int output_length = 0;
    size_t* length = malloc(sizeof(size_t));
    int encrypted_length = input_length + TAG_SIZE;

    unsigned char key[KEY_SIZE];
    unsigned char encrypted_key[ENCRYPTED_KEY_SIZE];
    unsigned char encrypted[encrypted_length];
    unsigned char enc_with_key[ENCRYPTED_KEY_SIZE + encrypted_length];

    // Initialize
    memset(key, 0, KEY_SIZE);
    memset(encrypted, 0, encrypted_length);
    memset(encrypted_key, 0, ENCRYPTED_KEY_SIZE);
    memset(enc_with_key, 0, ENCRYPTED_KEY_SIZE + encrypted_length);

    // Generate a random AES key, make sure there's no '\0' in key
    while (strlen(key) != KEY_SIZE)
        if (generate_random_aes_key(key) != 0)
            return -1;

    // Encrypt AES256 key by RSA
    if (privateEncryptCode(encrypted_key, key, length) != 0)
        return -1;
    // Encrypt plain text
#ifdef DEBUG
    printf("\n");
    hex_print("ENC key", key, sizeof(key));
    hex_print("ENC encrypted key", encrypted_key, sizeof(encrypted_key));
#endif
    if (aes_gcm_encrypt_fix_iv(encrypted, input, input_length, key) != 0)
        return -1;
#ifdef DEBUG
    hex_print("ENC encrypted", encrypted, sizeof(encrypted));
#endif

    memset(enc_with_key, 0, ENCRYPTED_KEY_SIZE+encrypted_length);
    memcpy(enc_with_key, encrypted_key, ENCRYPTED_KEY_SIZE);
    memcpy(enc_with_key+ENCRYPTED_KEY_SIZE, encrypted, encrypted_length);

    // Base64 encode result
    if (b64encode_str(enc_with_key, output, &output_length, ENCRYPTED_KEY_SIZE+encrypted_length) != 0)
        return -1;
    return 0;
}
#endif

/*
 * @param unsigned char* input: Encrypted text going to encrypt.
 * @param unsigned char* output: input * 3 / 4 - ENCRYPTED_KEY_SIZE - TAG_SIZE + 1
 */
int teraPublicDecrypt(unsigned char* output, unsigned char* input)
{
    // Elimiate " from input string (not in base64 charactors)
    eliminate_str(input, '"');
    int input_length = strlen(input);
    int decoded_length;

    unsigned char decoded[input_length];

    // Base64 decode
    if (b64decode_str(input, decoded, &decoded_length, input_length) != 0)
        return -1;

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
    hex_print("DEC key", key, KEY_SIZE);
    hex_print("DEC encrypted key", encrypted_key, ENCRYPTED_KEY_SIZE);
    hex_print("DEC encrypted", encrypted, encrypted_length);
#endif
    // Start to decrypt
    if (aes_gcm_decrypt_fix_iv(output, encrypted, encrypted_length, key) != 0) {
        printf("error\n");
        return -1;
    }
    output[encrypted_length - TAG_SIZE] = '\0';

    return 0;
}

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
