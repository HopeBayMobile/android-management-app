#include <jni.h>
#include <sys/system_properties.h>

//extern const char * helloC(const char *str);

JNIEXPORT jstring JNICALL Java_com_hopebaytech_hcfsmgmt_utils_HCFSApiUtils_helloJNI(
		JNIEnv *jEnv, jobject jObject, jstring jString) {
//	const char *nativeString = (*jEnv)->GetStringUTFChars(jEnv, jString, 0);
//	return (*jEnv)->NewStringUTF(jEnv, helloC(nativeString));
//	return (*jEnv)->NewStringUTF(jEnv, nativeString);

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

}

