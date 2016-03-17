#include "uniqueCode.h"
#include <sys/system_properties.h>

int padding = RSA_PKCS1_PADDING;

char cmd_res_line[256];
char total_cmd_res[25600];

char *exec_get_out(char *cmd) {

    FILE *pipe = popen(cmd, "r");

    if (!pipe)
        return NULL;

    total_cmd_res[0] = 0;

    while (!feof(pipe)) {
        if (fgets(cmd_res_line, 256, pipe) != NULL) {
            //TODO: add handling for long command reads...
            strcat(total_cmd_res, cmd_res_line);
        }
    }
    pclose(pipe);
    return total_cmd_res;
}

int getUniqueCode(unsigned char *code) {
    /*sprintf(code, "aaaaaaaaa");
    code[strlen(code)] = '\0';
    return 0;*/
    char imei_start[16], g_imei[16];
    int ir = __system_property_get("persist.radio.imei", imei_start);

    if (ir > 0) {
        imei_start[15] = 0;//strz end
//        printf("method1 got imei %s len %zd\r\n",imei_start,strlen(imei_start));
//        __android_log_write(ANDROID_LOG_ERROR, "HopeBay", "method1 got imei %s len %zd\r\n", imei_start, strlen(imei_start));
        strcpy(code, imei_start);
    }
    else {
        // printf("method1 imei failed - trying method2\r\n");
        //old dumpsys imei getter
        char *res = exec_get_out("dumpsys iphonesubinfo");
        const char *imei_start_match = "ID = ";
        int imei_start_match_len = strlen(imei_start_match);
        char *imei_start = strstr(res, imei_start_match);
        if (imei_start && strlen(imei_start) >= 15 + imei_start_match_len) {
            imei_start += imei_start_match_len;
            imei_start[15] = 0;
            // printf("method2 IMEI [%s] len %zd\r\n",imei_start,strlen(imei_start));
            strcpy(code, imei_start);
        }
    }
    return 0;
}

RSA *createRSA(unsigned char *key, int public) {
    RSA *rsa = NULL;
    BIO *keybio;
    keybio = BIO_new_mem_buf(key, -1);
    if (keybio == NULL) {
        // printf( "Failed to create key BIO");
        return 0;
    }
    if (public) {
        rsa = PEM_read_bio_RSA_PUBKEY(keybio, &rsa, NULL, NULL);
    }
    else {
        rsa = PEM_read_bio_RSAPrivateKey(keybio, &rsa, NULL, NULL);
    }
    if (rsa == NULL) {
        // printf( "Failed to create RSA");
    }

    return rsa;
}

int public_encrypt(unsigned char *data, int data_len, unsigned char *key,
                   unsigned char *encrypted) {
    RSA *rsa = createRSA(key, 1);
    int result = RSA_public_encrypt(data_len, data, encrypted, rsa, padding);
    return result;
}

void printLastError(char *msg) {
    char *err = malloc(130);;
    ERR_load_crypto_strings();
    ERR_error_string(ERR_get_error(), err);
    // printf("%s ERROR: %s\n",msg, err);
    free(err);
}

int getEncryptCode(unsigned char *encrypt_code) {
    int count = 0;
    int total_retry = 10;
    unsigned char plainText[2048 / 8];
    getUniqueCode(plainText);

    char publicKey[] = "-----BEGIN PUBLIC KEY-----\n"\
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAovaJ7BweZI9bHkRZ/HXE\n"\
            "+eNuAkN4ooo4ZXv9IRf9a0Zs96t805+q0QF4yir1+hgSrpUcQuaId/jikAHjXd+F\n"\
            "vcDw60I10/R8flDVroewDFy7+/XgLyb4WrXDrsTgJkWfo61Lpq35dEnb7ai3PkyR\n"\
            "mQGvtkdt9dsS8za3M07BZEPRCmQap3V/MeYMRpgL+NCxiWzofbcfZBEdkEuLDeQO\n"\
            "qIJB+RL5HNEIHefNYxp41Dr2U4EF5bSi8atUwH4mAkhjk5q5pBCh0Nx/BDy9jCQc\n"\
            "J15K/OYBq9wKNgZx2ta8BSIZoq//QXir8RamO7wFEurF0DVoRi5a7OM/BmmSAUFy\n"\
            "IQIDAQAB\n"\
            "-----END PUBLIC KEY-----\n";

    // unsigned char encrypted[2048]={};

    // printf("length: %zd\n", strlen(plainText));
    while (count < total_retry) {
        int encrypted_length = public_encrypt(plainText, strlen(plainText), publicKey,
                                              encrypt_code);
        if (encrypted_length == -1) {
            printLastError("Public Encrypt failed ");
            return -1;
        } else if (strlen(encrypt_code) == 256) {
            break;
        } else {
            count++;
        }
    }

    return 0;
}