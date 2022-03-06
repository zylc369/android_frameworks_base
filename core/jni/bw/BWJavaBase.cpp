#include "stdafx.h"
#include "BWJavaBase.h"

//////////////////////////////////////////////////////////////////////////
// BWJObject

BWJObject::BWJObject(JNIEnv* env, jobject obj) : mEnv(env), mJObject(NULL), mClass(NULL) {
    jclass clazz = env->GetObjectClass(obj);

    mJObject = env->NewGlobalRef(obj);
    mClass = (jclass) env->NewGlobalRef(clazz);

    env->DeleteLocalRef(clazz);
}

BWJObject::~BWJObject() {
    if (NULL != mClass) {
        mEnv->DeleteGlobalRef(mClass);
    }
    if (NULL != mJObject) {
        mEnv->DeleteGlobalRef(mJObject);
    }
}

jobject BWJObject::GetJObject() {
    return mJObject;
}

void BWJObject::CallVoidMethod(const char* methodName, const char* sig, ...) {
    jmethodID id = BWGetMethodID(mEnv, mJObject, methodName, sig);

    va_list ap;
    va_start(ap, sig);
    mEnv->CallVoidMethodV(mJObject, id, ap);
    va_end(ap);
}

jboolean BWJObject::CallBooleanMethod(const char* methodName, const char* sig, ...)  {
    jmethodID id = BWGetMethodID(mEnv, mJObject, methodName, sig);
    
    va_list ap;
    va_start(ap, sig);
    jboolean result = mEnv->CallBooleanMethodV(mJObject, id, ap);
    va_end(ap);
    return result;
}

jbyte BWJObject::CallByteMethod(const char* methodName, const char* sig, ...) {
    jmethodID id = BWGetMethodID(mEnv, mJObject, methodName, sig);

    va_list ap;
    va_start(ap, sig);
    jbyte result = mEnv->CallByteMethodV(mJObject, id, ap);
    va_end(ap);
    return result;
}

jshort BWJObject::CallShortMethod(const char* methodName, const char* sig, ...) {
    jmethodID id = BWGetMethodID(mEnv, mJObject, methodName, sig);

    va_list ap;
    va_start(ap, sig);
    jshort result = mEnv->CallShortMethodV(mJObject, id, ap);
    va_end(ap);
    return result;
}

jchar BWJObject::CallCharMethod(const char* methodName, const char* sig, ...) {
    jmethodID id = BWGetMethodID(mEnv, mJObject, methodName, sig);
    va_list ap;
    va_start(ap, sig);
    jchar result = mEnv->CallCharMethodV(mJObject, id, ap);
    va_end(ap);
    return result;
}

jint BWJObject::CallIntMethod(const char* methodName, const char* sig, ...) {
    jmethodID id = BWGetMethodID(mEnv, mJObject, methodName, sig);
    va_list ap;
    va_start(ap, sig);
    jint result = mEnv->CallIntMethodV(mJObject, id, ap);
    va_end(ap);
    return result;
}

jlong BWJObject::CallLongMethod(const char* methodName, const char* sig, ...) {
    jmethodID id = BWGetMethodID(mEnv, mJObject, methodName, sig);
    va_list ap;
    va_start(ap, sig);
    jlong result = mEnv->CallLongMethodV(mJObject, id, ap);
    va_end(ap);
    return result;
}

jfloat BWJObject::CallFloatMethod(const char* methodName, const char* sig, ...) {
    jmethodID id = BWGetMethodID(mEnv, mJObject, methodName, sig);
    va_list ap;
    va_start(ap, sig);
    jfloat result = mEnv->CallFloatMethodV(mJObject, id, ap);
    va_end(ap);
    return result;
}

jdouble BWJObject::CallDoubleMethod(const char* methodName, const char* sig, ...) {
    jmethodID id = BWGetMethodID(mEnv, mJObject, methodName, sig);
    va_list ap;
    va_start(ap, sig);
    jdouble result = mEnv->CallDoubleMethodV(mJObject, id, ap);
    va_end(ap);
    return result;
}
