#include <jni.h>

extern const char * helloC(const char *str);

JNIEXPORT jstring JNICALL Java_com_example_hcfsmgmt_utils_HCFSApiUtils_helloJNI(JNIEnv *jEnv, jobject jObject, jstring jString) {
	const char *nativeString = (*jEnv)->GetStringUTFChars(jEnv, jString, 0);
	return (*jEnv)->NewStringUTF(jEnv, helloC(nativeString));
//	return (*jEnv)->NewStringUTF(jEnv, nativeString);
}

