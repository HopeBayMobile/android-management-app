#include <jni.h>
#include <stdlib.h>
#include <sys/system_properties.h>
#include <android/log.h>
#include "uniqueCode.h"

extern void HCFS_file_status(const char **json_res, const char *pathname);
extern void HCFS_dir_status(const char **json_res, const char *pathname);
extern void HCFS_get_config(const char **json_res, const char *key);
//extern void HCFS_pin_path(const char **json_res, const char *pin_path);
extern void HCFS_pin_path(const char **json_res, const char *pin_path, const char pin_type);
extern void HCFS_pin_status(const char **json_res, const char *pathname);
extern void HCFS_set_config(const char **json_res, const char *key, const char *value);
extern void HCFS_stat(const char **json_res);
extern void HCFS_unpin_path(const char **json_res, const char *unpin_path);
extern void HCFS_reload_config(const char **json_res);
extern void HCFS_reset_xfer(const char **json_res);
extern void HCFS_toggle_sync(const char **json_res, int enabled);
extern void HCFS_get_sync_status(const char **json_res);
extern void HCFS_get_property(const char **json_res, const char *key);
extern void HCFS_set_property(const char **json_res, const char *key, const char *value);
extern void HCFS_get_occupied_size(const char **json_res);
extern void HCFS_set_sync_point(const char **json_res);
extern void HCFS_clear_sync_point(const char **json_res);
extern void HCFS_set_notify_server(const char **json_res, const char *pathname);
extern void HCFS_set_swift_token(const char **json_res, const char *url, const char *token);

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_getFileStatus(
		JNIEnv *jEnv, jobject jObject, jstring jFilePath) {
	const char *json_res;
	const char *pathname = (*jEnv)->GetStringUTFChars(jEnv, jFilePath, 0);
	HCFS_file_status(&json_res, pathname);
	jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
	free((char *)json_res);
	free((char *)pathname);
	return result;
}

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_getDirStatus(
		JNIEnv *jEnv, jobject jObject, jstring jFilePath) {
	const char *json_res;
    const char *pathname = (*jEnv)->GetStringUTFChars(jEnv, jFilePath, 0);
	HCFS_dir_status(&json_res, pathname);
	jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
	free((char *)json_res);
	free((char *)pathname);
	return result;
}

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_getHCFSConfig(
		JNIEnv *jEnv, jobject jObject, jstring jKey) {
	const char *json_res;
	const char *key = (*jEnv)->GetStringUTFChars(jEnv, jKey, 0);
	HCFS_get_config(&json_res, key);
	jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
	free((char *)json_res);
	free((char *)key);
	return result;
}

//JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_pin(
//		JNIEnv *jEnv, jobject jObject, jstring jString) {
//	const char *json_res;
//	const char *pin_path = (*jEnv)->GetStringUTFChars(jEnv, jString, 0);
//	HCFS_pin_path(&json_res, pin_path);
//	jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
//	free((char *)json_res);
//	free((char *)pin_path);
//	return result;
//}

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_pin(
		JNIEnv *jEnv, jobject jObject, jstring pinPath, jint pinType) {
	const char *json_res;
	const char *pin_path = (*jEnv)->GetStringUTFChars(jEnv, pinPath, 0);
	HCFS_pin_path(&json_res, pin_path, (char) pinType);
	jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
	free((char *)json_res);
	free((char *)pin_path);
	return result;
}

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_unpin(
		JNIEnv *jEnv, jobject jObject, jstring jString) {
	const char *json_res;
	const char *unpin_path = (*jEnv)->GetStringUTFChars(jEnv, jString, 0);
	HCFS_unpin_path(&json_res, unpin_path);
	jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
	free((char *)json_res);
	free((char *)unpin_path);
	return result;
}

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_getPinStatus(
		JNIEnv *jEnv, jobject jObject, jstring jString) {
	const char *json_res;
	const char *pathname = (*jEnv)->GetStringUTFChars(jEnv, jString, 0);
	HCFS_pin_status(&json_res, pathname);
	jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
	free((char *)json_res);
	free((char *)pathname);
	return result;
}

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_setHCFSConfig(
		JNIEnv *jEnv, jobject jObject, jstring jKey, jstring jValue) {
	const char *json_res;
    const char *key = (*jEnv)->GetStringUTFChars(jEnv, jKey, 0);
    const char *value = (*jEnv)->GetStringUTFChars(jEnv, jValue, 0);
	HCFS_set_config(&json_res, key, value);
	jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
	free((char *)json_res);
	free((char *)key);
	free((char *)value);
	return result;
}

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_setHCFSSyncStatus(
		JNIEnv *jEnv, jobject jObject, jint jInit) {
	const char *json_res;
	int enabled = jInit;
	HCFS_toggle_sync(&json_res, enabled);
	jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
	free((char *)json_res);
	return result;
}

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_getHCFSSyncStatus(
		JNIEnv *jEnv, jobject jObject, jint jInit) {
	const char *json_res;
	HCFS_get_sync_status(&json_res);
	jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
	free((char *)json_res);
	return result;
}

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_getHCFSStat(
		JNIEnv *jEnv, jobject jObject, jstring jString) {
	const char *json_res;
	HCFS_stat(&json_res);
	jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
	free((char *)json_res);
	return result;
}

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_reloadConfig(
		JNIEnv *jEnv, jobject jObject) {
	const char *json_res;
	HCFS_reload_config(&json_res);
	jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
	free((char *)json_res);
	return result;
}

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_resetXfer(
		JNIEnv *jEnv, jobject jObject) {
	const char *json_res;
	HCFS_reset_xfer(&json_res);
	jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
	free((char *)json_res);
	return result;
}

/*
JNIEXPORT jbyteArray JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_getEncryptedIMEI(
		JNIEnv *jEnv, jobject jObject) {
	int len = 4098;
	unsigned char encrypt_code[len];
	size_t* output_length = malloc(sizeof(size_t));
	int ret = getEncryptCode(encrypt_code, output_length);
    jbyteArray result = (*jEnv)->NewByteArray(jEnv, strlen(encrypt_code));
    (*jEnv)->SetByteArrayRegion(jEnv, result, 0, strlen(encrypt_code), encrypt_code);
//    __android_log_print(ANDROID_LOG_DEBUG, "HopeBay", "JNI: encrypt_code=%s", (unsigned char*) encrypt_code);

//    unsigned char decrypt_code[len];
//    decryptCode(decrypt_code, encrypt_code, output_length);
//    __android_log_print(ANDROID_LOG_ERROR, "HopeBay", "JNI: decrypt_code=%s", (unsigned char*) decrypt_code);

    free(output_length);
    return result;
}
*/

JNIEXPORT jbyteArray JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_getEncryptedIMEI(
		JNIEnv *jEnv, jobject jObject, jstring jImei) {
	int len = 4098;
	unsigned char encrypt_code[len];
	const char *imei = (*jEnv)->GetStringUTFChars(jEnv, jImei, 0);
	size_t* output_length = malloc(sizeof(size_t));
	int ret = encryptCode(encrypt_code, imei, output_length);
    jbyteArray result = (*jEnv)->NewByteArray(jEnv, strlen(encrypt_code));
    (*jEnv)->SetByteArrayRegion(jEnv, result, 0, strlen(encrypt_code), encrypt_code);
    //__android_log_print(ANDROID_LOG_DEBUG, "HopeBay", "JNI: jImei=%s", (char*) imei);
    //__android_log_print(ANDROID_LOG_DEBUG, "HopeBay", "JNI: encrypt_code=%s", (unsigned char*) encrypt_code);

    //unsigned char decrypt_code[len];
    //decryptCode(decrypt_code, encrypt_code, output_length);
    //__android_log_print(ANDROID_LOG_ERROR, "HopeBay", "JNI: decrypt_code=%s", (unsigned char*) decrypt_code);

    free(output_length);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_getOccupiedSize(
		JNIEnv *jEnv, jobject jObject, jint jInit) {
	const char *json_res;
	HCFS_get_occupied_size(&json_res);
	jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
	free((char *)json_res);
	return result;
}

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_setNotifyServer(
		JNIEnv *jEnv, jobject jObject, jstring jString) {
	const char *json_res;
    const char *pathname = (*jEnv)->GetStringUTFChars(jEnv, jString, 0);
    HCFS_set_notify_server(&json_res, pathname);
    jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
    free((char *)json_res);
    free((char *)pathname);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_startUploadTeraData(
		JNIEnv *jEnv, jobject jObject) {
	const char *json_res;
    HCFS_set_sync_point(&json_res);
    jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
    free((char *)json_res);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_stopUploadTeraData(
		JNIEnv *jEnv, jobject jObject) {
	const char *json_res;
    HCFS_clear_sync_point(&json_res);
    jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
    free((char *)json_res);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_setSwiftToken(
 		JNIEnv *jEnv, jobject jObject, jstring jUrl, jstring jToken) {
 	const char *json_res;
    const char *url = (*jEnv)->GetStringUTFChars(jEnv, jUrl, 0);
    const char *token = (*jEnv)->GetStringUTFChars(jEnv, jToken, 0);
    HCFS_set_swift_token(&json_res, url, token);
    jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
    free((char *)json_res);
    free((char *)url);
    free((char *)token);
    return result;
}
