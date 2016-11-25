#include <jni.h>
#include <stdlib.h>
#include <sys/system_properties.h>
#include <android/log.h>
#include "crypt.h"

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
extern void HCFS_trigger_restore(const char **json_res);
extern void HCFS_check_restore_status(const char **json_res);
extern void HCFS_notify_applist_change(const char **json_res);
extern void HCFS_check_package_boost_status(const char **json_res, const char *package_name);
extern void HCFS_enable_booster(const char **json_res, const long booster_size);
extern void HCFS_disable_booster(const char **json_res);
extern void HCFS_trigger_boost(const char **json_res);
extern void HCFS_trigger_unboost(const char **json_res);
extern void HCFS_clear_booster_package_remaining(const char **json_res, const char *package_name);
extern void HCFS_mount_smart_cache(const char **json_res);
extern void HCFS_umount_smart_cache(const char **json_res);

//extern void HCFS_collect_sys_logs(const char **json_res);

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
	int ret = publicEncryptCode(encrypt_code, imei, output_length);
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

JNIEXPORT jbyteArray JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_getDecryptedJsonString(
		JNIEnv *jEnv, jobject jObject, jstring jJsonString) {
	int len = 4098;
	unsigned char decrypt_code[len];
	const char *jsonString = (*jEnv)->GetStringUTFChars(jEnv, jJsonString, 0);
	int ret = teraPublicDecrypt(decrypt_code, jsonString);
    jbyteArray result = (*jEnv)->NewByteArray(jEnv, strlen(decrypt_code));
    (*jEnv)->SetByteArrayRegion(jEnv, result, 0, strlen(decrypt_code), decrypt_code);

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

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_collectSysLogs(
        JNIEnv *jEnv, jobject jObject) {
    const char *json_res;
    HCFS_collect_sys_logs(&json_res);
    jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
    free((char *)json_res);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_triggerRestore(
		JNIEnv *jEnv, jobject jObject) {
	const char *json_res;
	HCFS_trigger_restore(&json_res);
	jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
	free((char *)json_res);
	return result;
}

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_checkRestoreStatus(
		JNIEnv *jEnv, jobject jObject) {
	const char *json_res;
	HCFS_check_restore_status(&json_res);
	jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
	free((char *)json_res);
	return result;
}

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_notifyApplistChange(
		JNIEnv *jEnv, jobject jObject) {
	const char *json_res;
	HCFS_notify_applist_change(&json_res);
	jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
	free((char *)json_res);
	return result;
}

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_checkPackageBoostStatus(
		JNIEnv *jEnv, jobject jObject, jstring jpackageName) {
	const char *json_res;
    const char *package_name = (*jEnv)->GetStringUTFChars(jEnv, jpackageName, 0);
    HCFS_check_package_boost_status(&json_res, package_name);
    jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
    free((char *)json_res);
    free((char *)package_name);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_enableBooster(
		JNIEnv *jEnv, jobject jObject, jlong jboosterSize) {
	const char *json_res;
    const long booster_size = jboosterSize;
    HCFS_enable_booster(&json_res, booster_size);
    jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
    free((char *)json_res);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_disableBooster(
		JNIEnv *jEnv, jobject jObject) {
	const char *json_res;
    HCFS_disable_booster(&json_res);
    jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
    free((char *)json_res);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_triggerBoost(
		JNIEnv *jEnv, jobject jObject) {
	const char *json_res;
    HCFS_trigger_boost(&json_res);
    jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
    free((char *)json_res);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_triggerUnboost(
		JNIEnv *jEnv, jobject jObject) {
	const char *json_res;
    HCFS_trigger_unboost(&json_res);
    jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
    free((char *)json_res);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_clearBoosterPackageRemaining(
		JNIEnv *jEnv, jobject jObject, jstring jpackageName) {
	const char *json_res;
    const char *package_name = (*jEnv)->GetStringUTFChars(jEnv, jpackageName, 0);
    HCFS_clear_booster_package_remaining(&json_res, package_name);
    jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
    free((char *)json_res);
    free((char *)package_name);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_umountBooster(
		JNIEnv *jEnv, jobject jObject, jstring jpackageName) {
	const char *json_res;
    HCFS_umount_smart_cache(&json_res);
    jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
    free((char *)json_res);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_mountBooster(
		JNIEnv *jEnv, jobject jObject, jstring jpackageName) {
	const char *json_res;
    HCFS_mount_smart_cache(&json_res);
    jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
    free((char *)json_res);
    return result;
}
