#include <jni.h>
#include <sys/system_properties.h>

extern void HCFS_file_status(char **json_res, char *pathname);
extern void HCFS_get_config(char **json_res, char *key);
extern void HCFS_get_property(char **json_res, char *key);
extern void HCFS_pin_path(char **json_res, char *pin_path);
extern void HCFS_pin_status(char **json_res, char *pathname);
extern void HCFS_set_config(char **json_res, char *key, char *value);
extern void HCFS_set_property(char **json_res, char *key, char *value);
extern void HCFS_stat(char **json_res);
extern void HCFS_unpin_path(char **json_res, char *unpin_path);
extern void HCFS_reboot(char **json_res);
extern void HCFS_reload_config(char **json_res);
extern void HCFS_reset_xfer(char **json_res);
extern void HCFS_toggle_sync(char **json_res, int enabled);

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_getFileStatus(
		JNIEnv *jEnv, jobject jObject, jstring jFilePath) {
	char *json_res;
	char *pathname = (*jEnv)->GetStringUTFChars(jEnv, jFilePath, 0);
	HCFS_file_status(&json_res, pathname);
	jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
	free(json_res);
	free(pathname);
	return result;
}

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_getHCFSConfig(
		JNIEnv *jEnv, jobject jObject, jstring jKey) {
	char *json_res;
	char *key = (*jEnv)->GetStringUTFChars(jEnv, jKey, 0);
	HCFS_get_config(&json_res, key);
	jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
	free(json_res);
	free(key);
	return result;
}

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_pin(
		JNIEnv *jEnv, jobject jObject, jstring jString) {
	char *json_res;
	char *pin_path = (*jEnv)->GetStringUTFChars(jEnv, jString, 0);
	HCFS_pin_path(&json_res, pin_path);
	jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
	free(json_res);
	free(pin_path);
	return result;
}

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_unpin(
		JNIEnv *jEnv, jobject jObject, jstring jString) {
	char *json_res;
	char *unpin_path = (*jEnv)->GetStringUTFChars(jEnv, jString, 0);
	HCFS_unpin_path(&json_res, unpin_path);
	jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
	free(json_res);
	free(unpin_path);
	return result;
}

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_getPinStatus(
		JNIEnv *jEnv, jobject jObject, jstring jString) {
	char *json_res;
	char *pathname = (*jEnv)->GetStringUTFChars(jEnv, jString, 0);
	HCFS_pin_status(&json_res, pathname);
	jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
	free(json_res);
	free(pathname);
	return result;
}

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_setHCFSConfig(
		JNIEnv *jEnv, jobject jObject, jstring jKey, jstring jValue) {
	char *json_res;
	char *key = (*jEnv)->GetStringUTFChars(jEnv, jKey, 0);
	char *value = (*jEnv)->GetStringUTFChars(jEnv, jValue, 0);
	HCFS_set_config(&json_res, key, value);
	jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
	free(json_res);
	return result;
}

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_setHCFSSyncStatus(
		JNIEnv *jEnv, jobject jObject, jint jInit) {
	char *json_res;
	int enabled = jInit;
	HCFS_toggle_sync(&json_res, enabled);
	jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
	free(json_res);
	return result;
}

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_getHCFSStat(
		JNIEnv *jEnv, jobject jObject, jstring jString) {
	char *json_res;
	HCFS_stat(&json_res);
	jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
	free(json_res);
	return result;
}

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_reboot(
		JNIEnv *jEnv, jobject jObject) {
	char *json_res;
	HCFS_reboot(&json_res);
	jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
	free(json_res);
	return result;
}

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_reloadConfig(
		JNIEnv *jEnv, jobject jObject) {
	char *json_res;
	HCFS_reload_config(&json_res);
	jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
	free(json_res);
	return result;
}

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_resetXfer(
		JNIEnv *jEnv, jobject jObject) {
	char *json_res;
	HCFS_reset_xfer(&json_res);
	jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
	free(json_res);
	return result;
}

//JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_setHCFSProperty(
//		JNIEnv *jEnv, jobject jObject, jstring jKey, jstring jValue) {
//	char *json_res;
//	char *key = (*jEnv)->GetStringUTFChars(jEnv, jKey, 0);
//	char *value = (*jEnv)->GetStringUTFChars(jEnv, jValue, 0);
//	HCFS_set_property(&json_res, key, value);
//	jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
//	free(json_res);
//	return result;
//}

//JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_getHCFSProperty(
//		JNIEnv *jEnv, jobject jObject, jstring jKey) {
//	char *json_res;
//	char *key = (*jEnv)->GetStringUTFChars(jEnv, jKey, 0);
//	HCFS_get_property(&json_res, key);
//	jstring result = (*jEnv)->NewStringUTF(jEnv, json_res);
//	free(json_res);
//	free(key);
//	return result;
//}

//JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_helloJNI(
//		JNIEnv *jEnv, jobject jObject, jstring jString) {
//	const char *nativeString = (*jEnv)->GetStringUTFChars(jEnv, jString, 0);
//	return (*jEnv)->NewStringUTF(jEnv, helloC(nativeString));
//	return (*jEnv)->NewStringUTF(jEnv, nativeString);
//
//	char g_imei[32];
//	char imei_start[PROP_VALUE_MAX];
//	int ir = __system_property_get("ro.product.model", imei_start);
//	if (ir > 0) {
//		imei_start[15] = 0; //strz end
//		printf("method1 got imei %s len %d\r\n", imei_start,
//				strlen(imei_start));
//		strcpy(g_imei, imei_start);
//	}
//	return (*jEnv)->NewStringUTF(jEnv, g_imei);
//}
