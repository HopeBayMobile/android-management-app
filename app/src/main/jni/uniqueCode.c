#include "uniqueCode.h"
#include <sys/system_properties.h>

int padding = RSA_PKCS1_PADDING;
char publicKey[]="-----BEGIN PUBLIC KEY-----\n"\
                  "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAx6Kp1jzh9wVZ4BiwnW2G\n"\
                  "fhmMiZk1138w8RwmViZCqWmL0Pj9SPR6w2XMx1bVDkohVWgs4kjPrE44349QiAmp\n"\
                  "Lb9o8mSrQxGkJCKGAE+xYE5n1QO+4zPzs0eWJyXLJ8Pn4tvk7+qbK/Ybv06/8a8B\n"\
                  "zAhQObaPbS0hSwjqMR8kcy7OMFL/S5UxQ/Td41tDbqXYe/6MAF17jtJMRVv40nTX\n"\
                  "jdDh77Q5u7gK79HtYZPEsKO72CfAIxLU1f1hXqgtgXS3iSVFg6H4PFpDph889GsJ\n"\
                  "z9tnn1X/dNYJG/oP34bC7cWz8XdPeyBxB4sCdZKSy7AOryXw5vzcOeEd9OJY2Hdh\n"\
                  "+wIDAQAB\n"\
                  "-----END PUBLIC KEY-----\n";
// Disable private key functions
char privateKey[] = "";

char cmd_res_line[256];
char total_cmd_res[25600];

char* exec_get_out(char* cmd)
{

    FILE* pipe = popen(cmd, "r");

    if (!pipe)
        return NULL;

    total_cmd_res[0] = 0;

    while(!feof(pipe)) {
        if(fgets(cmd_res_line, 256, pipe) != NULL)
        {
            //TODO: add handling for long command reads...
            strcat(total_cmd_res,cmd_res_line);
        }
    }
    pclose(pipe);
    return total_cmd_res;
}

int getUniqueCode(unsigned char *code)
{

    int ir;
    char product_name[16];
    char imei_start[32];
    int pr = __system_property_get("ro.product.brand", product_name);

    if (pr > 0) {
        ir = __system_property_get("ro.aceruuid", imei_start);
    } else {
        ir = __system_property_get("ro.gsm.imei", imei_start);
    }

    if(ir > 0)
    {
        imei_start[15]=0;//strz end
        printf("method1 got imei %s len %zd\r\n",imei_start,strlen(imei_start));
        strcpy(code,imei_start);
    }
    else
    {
        printf("method1 imei failed - trying method2\r\n");
        //old dumpsys imei getter
        char* res = exec_get_out("dumpsys iphonesubinfo");
        const char* imei_start_match = "ID = ";
        int imei_start_match_len = strlen(imei_start_match);
        char* imei_start = strstr(res,imei_start_match);
        if(imei_start && strlen(imei_start)>=15+imei_start_match_len)
        {
            imei_start += imei_start_match_len;
            imei_start[15] = 0;
            printf("method2 IMEI [%s] len %zd\r\n",imei_start,strlen(imei_start));
            strcpy(code,imei_start);
        }
    }
    return 0;
}

RSA * createRSA(unsigned char * key,int public)
{
    RSA *rsa= NULL;
    BIO *keybio ;
    keybio = BIO_new_mem_buf(key, -1);
    if (keybio==NULL)
    {
        printf( "Failed to create key BIO");
        return 0;
    }
    if(public)
    {
        rsa = PEM_read_bio_RSA_PUBKEY(keybio, &rsa,NULL, NULL);
    }
    else
    {
        rsa = PEM_read_bio_RSAPrivateKey(keybio, &rsa,NULL, NULL);
    }
    if(rsa == NULL)
    {
        printf( "Failed to create RSA");
    }

    return rsa;
}

int public_encrypt(unsigned char * data,int data_len,unsigned char * key, unsigned char *encrypted)
{
    RSA * rsa = createRSA(key,1);
    int result = RSA_public_encrypt(data_len,data,encrypted,rsa,padding);
    return result;
}

int public_decrypt(unsigned char * enc_data,int data_len,unsigned char * key, unsigned char *decrypted)
{
    RSA * rsa = createRSA(key,1);
    int  result = RSA_public_decrypt(data_len,enc_data,decrypted,rsa,padding);
    return result;
}

int private_encrypt(unsigned char * data,int data_len,unsigned char * key, unsigned char *encrypted)
{
    RSA * rsa = createRSA(key,0);
    int result = RSA_private_encrypt(data_len,data,encrypted,rsa,padding);
    return result;
}

int private_decrypt(unsigned char * enc_data,int data_len,unsigned char * key, unsigned char *decrypted)
{
    RSA * rsa = createRSA(key,0);
    int  result = RSA_private_decrypt(data_len,enc_data,decrypted,rsa,padding);
    return result;
}

void printLastError(char *msg)
{
    char * err = malloc(130);;
    ERR_load_crypto_strings();
    ERR_error_string(ERR_get_error(), err);
    printf("%s ERROR: %s\n",msg, err);
    free(err);
}

int getEncryptCode(unsigned char* encrypted, size_t* output_length)
{

    unsigned char plainText[2048/8];
    getUniqueCode(plainText);
    publicEncryptCode(encrypted, plainText, output_length);

    return 0;
}

int encryptCode(char* keyType, unsigned char* encrypted, const unsigned char* plainText, const size_t* output_length)
{
    // Encrypt and base64 encode <plainText> into <encrypted> and return len of <encryted> to <output_length>
    // @unsigned char* encrypted: 4098
    // @unsigned char* plainText: 256

    int count = 0;
    int total_retry = 20;
    int encrypted_length = -1;
    unsigned char encrypt_code[4098] = {};
    unsigned char* encoded;

    while (strlen(encrypt_code) != encrypted_length) {
        if (strcmp(keyType, "public") == 0)
        {
            encrypted_length = public_encrypt(plainText,strlen(plainText),publicKey,encrypt_code);
        }
        else if (strcmp(keyType, "private") == 0)
        {
            encrypted_length = private_encrypt(plainText,strlen(plainText),privateKey,encrypt_code);
        }
        else
        {
            printLastError("Not supprt key type");
            return -1;
        }

        if (encrypted_length == -1) {
            printLastError("Public Encrypt failed ");
            return -1;
        }
    }
    encoded = base64_encode(encrypt_code, strlen(encrypt_code), output_length);
    strcpy(encrypted, encoded);
    return 0;
}

int decryptCode(char* keyType, unsigned char* decrypted, const unsigned char* encrypted, const size_t input_length)
{
    //
    // base64 decode and decrypt <encrypted> into <decrypted>, length of <encrypted> is <input_length>
    //
    // @unsigend char* decrypted: 4098
    // @unsigend char* encrypted: 4098

    size_t* output_length = malloc(sizeof(size_t));
    unsigned char decrypt_code[4098] = {};
    unsigned char* decoded;
    int decrypted_length;

    decoded = base64_decode(encrypted, input_length, output_length);
    if (strcmp(keyType, "public") == 0)
    {
        decrypted_length = public_decrypt(decoded, strlen(decoded), publicKey, decrypt_code);
    }
    else if (strcmp(keyType, "private") == 0)
    {
        decrypted_length = private_decrypt(decoded, strlen(decoded), privateKey, decrypt_code);
    }
    else
    {
        printLastError("Not supprt key type");
        return -1;
    }
    if(decrypted_length == -1)
    {
        printLastError("Private Decrypt failed ");
        return -1;
        //exit(0);
    }
    strcpy(decrypted, decrypt_code);
    free(output_length);
    return 0;
}

int publicEncryptCode(unsigned char* encrypted, const unsigned char* plainText, const size_t* output_length)
{
    return encryptCode("public", encrypted, plainText, output_length);
}

int publicDecryptCode(unsigned char* decrypted, const unsigned char* encrypted, const size_t input_length)
{
    return decryptCode("public", decrypted, encrypted, input_length);
}
