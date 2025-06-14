/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class org_spcgreenville_deagan_physical_GPIOController */

#ifndef _Included_org_spcgreenville_deagan_physical_GPIOController
#define _Included_org_spcgreenville_deagan_physical_GPIOController
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     org_spcgreenville_deagan_physical_GPIOController
 * Method:    initializeOutput
 * Signature: (Ljava/lang/String;IZ)J
 */
JNIEXPORT jlong JNICALL Java_org_spcgreenville_deagan_physical_GPIOController_initializeOutput
  (JNIEnv *, jobject, jstring, jint, jboolean);

/*
 * Class:     org_spcgreenville_deagan_physical_GPIOController
 * Method:    initializeInput
 * Signature: (Ljava/lang/String;II)J
 */
JNIEXPORT jlong JNICALL Java_org_spcgreenville_deagan_physical_GPIOController_initializeInput
  (JNIEnv *, jobject, jstring, jint, jint);

/*
 * Class:     org_spcgreenville_deagan_physical_GPIOController
 * Method:    setInternal
 * Signature: (JZ)I
 */
JNIEXPORT jint JNICALL Java_org_spcgreenville_deagan_physical_GPIOController_setInternal
  (JNIEnv *, jobject, jlong, jboolean);

/*
 * Class:     org_spcgreenville_deagan_physical_GPIOController
 * Method:    getInternal
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_org_spcgreenville_deagan_physical_GPIOController_getInternal
  (JNIEnv *, jobject, jlong);

#ifdef __cplusplus
}
#endif
#endif
