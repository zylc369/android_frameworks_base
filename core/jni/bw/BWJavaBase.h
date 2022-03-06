#pragma once

#include <jni.h>
#include <JNIJava/JNIReflect.h>

#define DEFINE_JAVA_CLASS(JavaClass) \
    JavaClass(JNIEnv* env, jobject obj) : JavaClass##Super(env, obj) {}

//////////////////////////////////////////////////////////////////////////
// BWJObject

class BWJObject {
public:
    BWJObject(JNIEnv* env, jobject obj);

    virtual ~BWJObject();

    jobject GetJObject();

    /**
     * 创建对象。
     * @param[in] classDesc 类描述符。
     * @return
     */
    template<typename T>
    static Lsp<T> New(JNIEnv* env, const char* classDesc) {
        return New<T>(env, classDesc, "()V");
    }
    
    template<typename T>
    static Lsp<T> New(JNIEnv* env, const char* classDesc, const char* sig, ...) {
        jclass clazz = env->FindClass(classDesc);
        jmethodID id = env->GetMethodID(clazz, "<init>", sig);

        va_list ap;
        va_start(ap, sig);
        jobject result = env->NewObjectV(clazz, id, ap);
        va_end(ap);

        env->DeleteLocalRef(clazz);
        Lsp<T> o = new T(env, result);
        env->DeleteLocalRef(result);
        return o;
    }

    void CallVoidMethod(const char* methodName, const char* sig, ...);
    jboolean CallBooleanMethod(const char* methodName, const char* sig, ...);
    jbyte CallByteMethod(const char* methodName, const char* sig, ...);
    jshort CallShortMethod(const char* methodName, const char* sig, ...);
    jchar CallCharMethod(const char* methodName, const char* sig, ...);
    jint CallIntMethod(const char* methodName, const char* sig, ...);
    jlong CallLongMethod(const char* methodName, const char* sig, ...);
    jfloat CallFloatMethod(const char* methodName, const char* sig, ...);
    jdouble CallDoubleMethod(const char* methodName, const char* sig, ...);

    /**
     * 调用返回对象的方法。
     * @param[out] obj 返回对象。此对象需要释放内存。
     * @param[in] methodName 方法名。
     * @param[in] sig 方法签名。
     * @param[in] ... 方法参数。
     */
    template<typename T>
    Lsp<T> CallObjectMethod(const char* methodName, const char* sig, ...) {
        jmethodID id = BWGetMethodID(mEnv, mJObject, methodName, sig);
        
        va_list ap;
        va_start(ap, sig);
        jobject result = mEnv->CallObjectMethodV(mJObject, id, ap);
        va_end(ap);
        
        Lsp<T> o = new T(mEnv, result);
        mEnv->DeleteLocalRef(result);
        return o;
    }

protected:
    JNIEnv* mEnv;
    jobject mJObject;
    jclass mClass;
};
