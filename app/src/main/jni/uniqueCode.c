#include "uniqueCode.h"
#include <sys/system_properties.h>

int padding = RSA_PKCS1_PADDING;

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
    int count = 0;
    int total_retry = 20;
    int encrypted_length = -1;
    unsigned char plainText[2048/8];
    unsigned char encrypt_code[4098] = {};
    unsigned char* encoded;
    char publicKey[]="-----BEGIN PUBLIC KEY-----\n"\
			  "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAx6Kp1jzh9wVZ4BiwnW2G\n"\
			  "fhmMiZk1138w8RwmViZCqWmL0Pj9SPR6w2XMx1bVDkohVWgs4kjPrE44349QiAmp\n"\
			  "Lb9o8mSrQxGkJCKGAE+xYE5n1QO+4zPzs0eWJyXLJ8Pn4tvk7+qbK/Ybv06/8a8B\n"\
			  "zAhQObaPbS0hSwjqMR8kcy7OMFL/S5UxQ/Td41tDbqXYe/6MAF17jtJMRVv40nTX\n"\
			  "jdDh77Q5u7gK79HtYZPEsKO72CfAIxLU1f1hXqgtgXS3iSVFg6H4PFpDph889GsJ\n"\
			  "z9tnn1X/dNYJG/oP34bC7cWz8XdPeyBxB4sCdZKSy7AOryXw5vzcOeEd9OJY2Hdh\n"\
			  "+wIDAQAB\n"\
			  "-----END PUBLIC KEY-----\n";

    getUniqueCode(plainText);

    while (count < total_retry) {
        encrypted_length = public_encrypt(plainText,strlen(plainText),publicKey,encrypt_code);
        if (encrypted_length == -1) {
            printLastError("Public Encrypt failed ");
            return -1;
        }
        else if (strlen(encrypt_code) == encrypted_length) {
            break;
        }
        else {
            count++;
        }
    }
    // printf("[CM] Encrypted length = %d\n", strlen(encrypt_code));
    encoded = base64_encode(encrypt_code, strlen(encrypt_code), output_length);
    strcpy(encrypted, encoded);
    // printf("[CM] Encrypted and encode length = %d\n", strlen(encrypted));
    return 0;
}

int decryptCode(unsigned char* decrypted, unsigned char* encrypted, size_t* input_length)
{
    /*
     * Decrypt <encrypt_code> to <decrypt_code>
     */

    size_t* output_length = malloc(sizeof(output_length));
    unsigned char decrypt_code[4096] = {};
    unsigned char* decoded;
    char privateKey[] = "-----BEGIN RSA PRIVATE KEY-----\n"\
			     "MIIEowIBAAKCAQEAx6Kp1jzh9wVZ4BiwnW2GfhmMiZk1138w8RwmViZCqWmL0Pj9\n"\
			     "SPR6w2XMx1bVDkohVWgs4kjPrE44349QiAmpLb9o8mSrQxGkJCKGAE+xYE5n1QO+\n"\
			     "4zPzs0eWJyXLJ8Pn4tvk7+qbK/Ybv06/8a8BzAhQObaPbS0hSwjqMR8kcy7OMFL/\n"\
			     "S5UxQ/Td41tDbqXYe/6MAF17jtJMRVv40nTXjdDh77Q5u7gK79HtYZPEsKO72CfA\n"\
			     "IxLU1f1hXqgtgXS3iSVFg6H4PFpDph889GsJz9tnn1X/dNYJG/oP34bC7cWz8XdP\n"\
			     "eyBxB4sCdZKSy7AOryXw5vzcOeEd9OJY2Hdh+wIDAQABAoIBADDH8JQnAFgp+JIn\n"\
			     "tlBhiPGbMJoW0+86Zy0jYcK/Sp626bFAhjOjebPxVh6HezwopQiHkiAhBo3l90O5\n"\
			     "c0YqhiplkTFZo/eZgfBKd0/wKTmNLxX/+k7uTOhL38blF0U6O5lVfhWZ0Bzn4FPY\n"\
			     "3FIkH15J0cCoeVeGJZJ+NSuXnoJ+SNml84CslBEKEYf7OKBeljk6V4C6mGcraPVB\n"\
			     "5TszW8Ccv7B+ICEnDK54jupC21ZUtkSAGrvacQUwHojyxkeboO5EMk8eEHDkCklM\n"\
			     "Kjz5sF1a/EouM8fca5JO8Y5DULd9hWxg6lF8u/e67fLXm6yG3GA2dqLDCDJcECJH\n"\
			     "9HwGGUECgYEA6n443SYC3K7kKHhV200masJoTyCD+Tlavfk57hA4644VI5NPvHe8\n"\
			     "TvVMkGaVh4ggFuR0PkgOzY1DG/HIg3TCd5lhD5tSfAZnGe2AVJB5IKoQYQsA3Z07\n"\
			     "yUHtprXmLY2HyvxUiTUAKQKzn+rPYsGWU7U5IklG/dq1GWp+D4PlNnECgYEA2fIA\n"\
			     "WHUNgbufEisId2NXOvTSbe8x1Gjos+r002XLJCydNJsHWEkUe7j1XvtI1geM3+vE\n"\
			     "JRr5FH6VD2hQDupLvSQFztczEbJYZklqMX4A2isD0E3j630eVb2uWey05ux3owlO\n"\
			     "BYIWuvueetgsfsjOGiPLgroTLf3orUBgBZ0rjSsCgYBotf6MelnS0+IcVEf6isP5\n"\
			     "7mAH3XwxQTRNGsqMjP/t5992qGR1w336QD11SenBwL6bml5yJVk+3rK1+szQLsZc\n"\
			     "A8i84F3/9hf6Ev04Rd9g/7AatYAodfrpjyAhTp6/frDBqtXRAzLUHVm6sm7zKYEI\n"\
			     "VidyMQibeRtfgxdRtFMZcQKBgFXEzewBzJn0eypMMx3Aw3BZLoLC8io0SvebDFQx\n"\
			     "KBuJTyiHpgFRaTUzWaTSYRyRhhgNEXjEv6cYFZMlqvPjsoCxr3Nx7xAUzoMaeycU\n"\
			     "/kLdULfmHz7qU0jMx9Ntutdx0bcgj0rNoiJdqUWQ0xnl7m4NDibZEXk1Bh9ASUmM\n"\
			     "S7pXAoGBAJvrIN8OB9iz5f0EoXhe0WuWYhK7ydEVUr7Mwq9dV/bT/H8n6c2zKVql\n"\
			     "BQB9LzicBLuuvftWxO5r65x0NfT7oBDMq/wqWdgfipfyl5E9f+7FchSX/WPOQ8xe\n"\
			     "ozxqvnQleBB/gg0o02FIG9+cMD5EnGju8DS+KyTLwngZSkgvCcdz\n"\
			     "-----END RSA PRIVATE KEY-----\n";

    decoded = base64_decode(encrypted, *input_length, output_length);
    // printf("[CM] After decode length = %d\n", strlen(decrypt_code));
    int decrypted_length = private_decrypt(decoded, 256, privateKey, decrypt_code);
    if(decrypted_length == -1)
    {
        printLastError("Private Decrypt failed ");
        return -1;
        //exit(0);
    }
    strcpy(decrypted, decrypt_code);
    // printf("[CM] After decrypte and decode length = %d\n", strlen(decrypted));
    free(output_length);
    return 0;
}