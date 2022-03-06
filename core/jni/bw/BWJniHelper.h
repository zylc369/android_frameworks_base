#pragma once

#include <jni.h>
#include <SmartPointer.h>

/**
 * 下面三个函数是从art/runtime/utils.cc文件中完全拷贝出来的，为了防止符号冲突将原函数名的后面添加了"BW"后缀。
 */
// Tests for whether 's' is a valid class name in the three common forms:
bool IsValidBinaryClassNameBW(const char* s);  // "java.lang.String"
bool IsValidJniClassNameBW(const char* s);     // "java/lang/String"
bool IsValidDescriptorBW(const char* s);       // "Ljava/lang/String;"

/**
 * 类描述符转换为jni类名。
 */
bool ClassDescriptorToJniClassName(const char* classDesc, char** jniClassName);

/**
 * 判断方法是否存在。
 */
bool IsMethodExist(JNIEnv* env, const char* jniClassName, const char* methodName, const char* methodSig);

jboolean nativeInsertOrUpdateTraceMethodInfo(JNIEnv* env, jobject traceMethodInfoExt);

jboolean nativeInsertOrUpdateHookMethodInstInfo(JNIEnv* env, jobject hookMethodInstInfo);
