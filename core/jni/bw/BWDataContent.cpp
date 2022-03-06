#include "stdafx.h"
#include "BWDataContent.h"

//////////////////////////////////////////////////////////////////////////
// AppIDBase

Lsp<BWJAppIDBase> BWJAppIDBase::New(JNIEnv* env, jint uid, CString packageName, jint appType, jint startFlags) {
    jclass clazz = env->FindClass("android/bw/db/base/AppIDBase");
    jmethodID methodID = env->GetMethodID(clazz, "<init>", "(ILjava/lang/String;II)V");
    jstring jPackageName = CStringToJString(env, packageName.GetCString());
    jobject obj = env->NewObject(clazz, methodID, uid, jPackageName, appType, startFlags);
    Lsp<BWJAppIDBase> appIDBase = new BWJAppIDBase(env, obj);
    env->DeleteLocalRef(jPackageName);
    env->DeleteLocalRef(obj);
    env->DeleteLocalRef(clazz);
    return appIDBase;
}

Lsp<BWJAppIDBase> BWJAppIDBase::New(JNIEnv* env, Lsp<AppIDBase> nativeAppIDBase) {
    return BWJAppIDBase::New(env, nativeAppIDBase->uid, nativeAppIDBase->packageName, 
        nativeAppIDBase->appType, nativeAppIDBase->startFlags);
}

BWJAppIDBase::BWJAppIDBase(JNIEnv* env, jobject obj) : BWJObjectSuper(env, obj) {}

BWJAppIDBase::~BWJAppIDBase() {}

jint BWJAppIDBase::getUid() {
    jmethodID methodID = mEnv->GetMethodID(mClass, "getUid", "()I");
    return mEnv->CallIntMethod(mJObject, methodID);
}

void BWJAppIDBase::setUid(jint uid) {
    jmethodID methodID = mEnv->GetMethodID(mClass, "setUid", "(I)V");
    mEnv->CallVoidMethod(mJObject, methodID, uid);
}

CString BWJAppIDBase::getPackageName() {
    jmethodID methodID = mEnv->GetMethodID(mClass, "getPackageName", "()Ljava/lang/String;");
    jstring jStr = (jstring) mEnv->CallObjectMethod(mJObject, methodID);
    CString str = JStringToCString(mEnv, jStr);
    mEnv->DeleteLocalRef(jStr);
    return str;
}

void BWJAppIDBase::setPackageName(CString packageName) {
    jmethodID methodID = mEnv->GetMethodID(mClass, "getPackageName", "(Ljava/lang/String;)V");
    jstring jStr = CStringToJString(mEnv, packageName.GetCString());
    mEnv->CallVoidMethod(mJObject, methodID, jStr);
    mEnv->DeleteLocalRef(jStr);
}

jint BWJAppIDBase::getAppType() {
    jmethodID methodID = mEnv->GetMethodID(mClass, "getAppType", "()I");
    return mEnv->CallIntMethod(mJObject, methodID);
}

void BWJAppIDBase::setAppType(jint appType) {
    jmethodID methodID = mEnv->GetMethodID(mClass, "setAppType", "(I)V");
    mEnv->CallVoidMethod(mJObject, methodID, appType);
}

jint BWJAppIDBase::getStartFlags() {
    jmethodID methodID = mEnv->GetMethodID(mClass, "getStartFlags", "()I");
    return mEnv->CallIntMethod(mJObject, methodID);
}

void BWJAppIDBase::setStartFlags(jint startFlags) {
    jmethodID methodID = mEnv->GetMethodID(mClass, "setStartFlags", "(I)V");
    mEnv->CallVoidMethod(mJObject, methodID, startFlags);
}

//////////////////////////////////////////////////////////////////////////
// MethodIDBase

Lsp<BWJMethodIDBase> BWJMethodIDBase::New(JNIEnv* env, CString classDesc, CString methodName, CString methodSig) {
    jclass clazz = env->FindClass("android/bw/db/base/MethodIDBase");
    jmethodID methodID = env->GetMethodID(clazz, "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
    jstring jClassDesc = CStringToJString(env, classDesc.GetCString());
    jstring jMethodName = CStringToJString(env, methodName.GetCString());
    jstring jMethodSig = CStringToJString(env, methodSig.GetCString());
    jobject obj = env->NewObject(clazz, methodID, jClassDesc, jMethodName, jMethodSig);
    Lsp<BWJMethodIDBase> methodIDBase = new BWJMethodIDBase(env, obj);
    env->DeleteLocalRef(jMethodSig);
    env->DeleteLocalRef(jMethodName);
    env->DeleteLocalRef(jClassDesc);
    env->DeleteLocalRef(obj);
    env->DeleteLocalRef(clazz);
    return methodIDBase;
}

Lsp<BWJMethodIDBase> BWJMethodIDBase::New(JNIEnv* env, Lsp<MethodIDBase> nativeMethodIDBase) {
    return BWJMethodIDBase::New(env, nativeMethodIDBase->classDesc, nativeMethodIDBase->methodName,
        nativeMethodIDBase->methodSig);
}

BWJMethodIDBase::BWJMethodIDBase(JNIEnv* env, jobject obj) : BWJMethodIDBaseSuper(env, obj) {}

BWJMethodIDBase::~BWJMethodIDBase() {}

CString BWJMethodIDBase::getClassDesc() {
    jmethodID methodID = mEnv->GetMethodID(mClass, "getClassDesc", "()Ljava/lang/String;");
    jstring jStr = (jstring) mEnv->CallObjectMethod(mJObject, methodID);
    CString str = JStringToCString(mEnv, jStr);
    mEnv->DeleteLocalRef(jStr);
    return str;
}

CString BWJMethodIDBase::getMethodName() {
    jmethodID methodID = mEnv->GetMethodID(mClass, "getMethodName", "()Ljava/lang/String;");
    jstring jStr = (jstring) mEnv->CallObjectMethod(mJObject, methodID);
    CString str = JStringToCString(mEnv, jStr);
    mEnv->DeleteLocalRef(jStr);
    return str;
}

CString BWJMethodIDBase::getMethodSig() {
    jmethodID methodID = mEnv->GetMethodID(mClass, "getMethodSig", "()Ljava/lang/String;");
    jstring jStr = (jstring) mEnv->CallObjectMethod(mJObject, methodID);
    CString str = JStringToCString(mEnv, jStr);
    mEnv->DeleteLocalRef(jStr);
    return str;
}

jint BWJMethodIDBase::getHash() {
    jmethodID methodID = mEnv->GetMethodID(mClass, "getHash", "()I");
    return mEnv->CallIntMethod(mJObject, methodID);
}

void BWJMethodIDBase::setClassDesc(CString classDesc) {
    jmethodID methodID = mEnv->GetMethodID(mClass, "setClassDesc", "(Ljava/lang/String;)V");
    jstring jStr = CStringToJString(mEnv, classDesc.GetCString());
    mEnv->CallVoidMethod(mJObject, methodID, jStr);
    mEnv->DeleteLocalRef(jStr);
}

void BWJMethodIDBase::setMethodName(CString methodName) {
    jmethodID methodID = mEnv->GetMethodID(mClass, "setMethodName", "(Ljava/lang/String;)V");
    jstring jStr = CStringToJString(mEnv, methodName.GetCString());
    mEnv->CallVoidMethod(mJObject, methodID, jStr);
    mEnv->DeleteLocalRef(jStr);
}

void BWJMethodIDBase::setMethodSig(CString methodSig) {
    jmethodID methodID = mEnv->GetMethodID(mClass, "setMethodSig", "(Ljava/lang/String;)V");
    jstring jStr = CStringToJString(mEnv, methodSig.GetCString());
    mEnv->CallVoidMethod(mJObject, methodID, jStr);
    mEnv->DeleteLocalRef(jStr);
}

//////////////////////////////////////////////////////////////////////////
// MethodLocation

Lsp<BWJMethodLocation> BWJMethodLocation::New(
    JNIEnv* env, Lsp<BWJAppIDBase> appIDBase, Lsp<BWJMethodIDBase> methodIDBase) {
    jclass clazz = env->FindClass("android/bw/db/base/MethodLocation");
    jmethodID methodID = env->GetMethodID(clazz, "<init>", 
        "(Landroid/bw/db/base/AppIDBase;Landroid/bw/db/base/MethodIDBase;)V");
    jobject obj = env->NewObject(clazz, methodID, appIDBase->GetJObject(), methodIDBase->GetJObject());
    Lsp<BWJMethodLocation> result = new BWJMethodLocation(env, obj);
    env->DeleteLocalRef(obj);
    env->DeleteLocalRef(clazz);
    return result;
}

Lsp<BWJMethodLocation> BWJMethodLocation::New(
    JNIEnv* env, Lsp<AppIDBase> nativeAppIDBase, Lsp<MethodIDBase> nativeMethodIDBase) {
    Lsp<BWJAppIDBase> appIDBase = BWJAppIDBase::New(env, nativeAppIDBase);
    Lsp<BWJMethodIDBase> methodIDBase = BWJMethodIDBase::New(env, nativeMethodIDBase);
    return BWJMethodLocation::New(env, appIDBase, methodIDBase);
}

BWJMethodLocation::BWJMethodLocation(JNIEnv* env, jobject obj) : BWJMethodLocationSuper(env, obj) {}

BWJMethodLocation::~BWJMethodLocation() {}

Lsp<BWJAppIDBase> BWJMethodLocation::getAppIDBase() {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "appIDBase", "Landroid/bw/db/base/AppIDBase;");
    jobject obj = mEnv->GetObjectField(mJObject, fieldID);
    Lsp<BWJAppIDBase> result = new BWJAppIDBase(mEnv, obj);
    mEnv->DeleteLocalRef(obj);
    return result;
}

Lsp<BWJMethodIDBase> BWJMethodLocation::getMethodIDBase() {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "methodIDBase", "Landroid/bw/db/base/MethodIDBase;");
    jobject obj = mEnv->GetObjectField(mJObject, fieldID);
    Lsp<BWJMethodIDBase> result = new BWJMethodIDBase(mEnv, obj);
    mEnv->DeleteLocalRef(obj);
    return result;
}

void BWJMethodLocation::setAppIDBase(BWJAppIDBase* appIDBase) {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "appIDBase", "Landroid/bw/db/base/AppIDBase;");
    mEnv->SetObjectField(mJObject, fieldID, appIDBase->GetJObject());
}

void BWJMethodLocation::setMethodIDBase(BWJMethodIDBase* methodIDBase) {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "methodIDBase", "Landroid/bw/db/base/MethodIDBase;");
     mEnv->SetObjectField(mJObject, fieldID, methodIDBase->GetJObject());
}

//////////////////////////////////////////////////////////////////////////
// InstructionLocation

Lsp<BWJInstructionLocation> BWJInstructionLocation::New(
    JNIEnv* env, Lsp<BWJAppIDBase> appIDBase, 
    Lsp<BWJMethodIDBase> methodIDBase, int64_t instLineNum, int64_t dexPC) {
    jclass clazz = env->FindClass("android/bw/db/base/InstructionLocation");
    jmethodID methodID = env->GetMethodID(clazz, "<init>", 
        "(Landroid/bw/db/base/AppIDBase;Landroid/bw/db/base/MethodIDBase;JJ)V");
    jobject obj = env->NewObject(clazz, methodID, appIDBase->GetJObject(), methodIDBase->GetJObject(),
        instLineNum, dexPC);
    Lsp<BWJInstructionLocation> result = new BWJInstructionLocation(env, obj);
    env->DeleteLocalRef(obj);
    env->DeleteLocalRef(clazz);
    return result;
}

Lsp<BWJInstructionLocation> BWJInstructionLocation::New(JNIEnv* env, Lsp<AppIDBase> nativeAppIDBase, 
        Lsp<MethodIDBase> nativeMethodIDBase, int64_t instLineNum, int64_t dexPC) {
    Lsp<BWJAppIDBase> appIDBase = BWJAppIDBase::New(env, nativeAppIDBase);
    Lsp<BWJMethodIDBase> methodIDBase = BWJMethodIDBase::New(env, nativeMethodIDBase);
    return BWJInstructionLocation::New(env, appIDBase, methodIDBase, instLineNum, dexPC);
}

Lsp<BWJInstructionLocation> BWJInstructionLocation::New(JNIEnv* env, Lsp<InstructionLocation> nativeInstructionLocation) {
    return BWJInstructionLocation::New(env, nativeInstructionLocation->appIDBase, 
        nativeInstructionLocation->methodIDBase, nativeInstructionLocation->instLineNum, 
        nativeInstructionLocation->dexPC);
}

BWJInstructionLocation::BWJInstructionLocation(JNIEnv* env, jobject obj)
    : BWJInstructionLocationSuper(env, obj) {}

BWJInstructionLocation::~BWJInstructionLocation() {}

int64_t BWJInstructionLocation::getInstLineNum() {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "instLineNum", "J");
    return mEnv->GetLongField(mJObject, fieldID);
}

int64_t BWJInstructionLocation::getDexPC() {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "dexPC", "J");
    return mEnv->GetLongField(mJObject, fieldID);
}

void BWJInstructionLocation::setInstLineNum(int64_t instLineNum) {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "instLineNum", "J");
    mEnv->SetLongField(mJObject, fieldID, instLineNum);
}

void BWJInstructionLocation::setDexPC(int64_t dexPC) {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "dexPC", "J");
    mEnv->SetLongField(mJObject, fieldID, dexPC);
}

//////////////////////////////////////////////////////////////////////////
// TraceMethodInfoBase

BWJTraceMethodInfoBase::BWJTraceMethodInfoBase(JNIEnv* env, jobject obj) : BWJTraceMethodInfoBaseSuper(env, obj) {}

BWJTraceMethodInfoBase::~BWJTraceMethodInfoBase() {}

Lsp<BWJTraceMethodInfoBase> BWJTraceMethodInfoBase::New(JNIEnv* env) {
    jclass clazz = env->FindClass("android/bw/db/base/TraceMethodInfoBase");
    jmethodID methodID = env->GetMethodID(clazz, "<init>", "()V");
    jobject obj = env->NewObject(clazz, methodID);
    Lsp<BWJTraceMethodInfoBase> result = new BWJTraceMethodInfoBase(env, obj);
    env->DeleteLocalRef(obj);
    env->DeleteLocalRef(clazz);
    return result;
}

Lsp<BWJMethodLocation> BWJTraceMethodInfoBase::getMethodLocation() {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "methodLocation", "Landroid/bw/db/base/MethodLocation;");
    jobject obj = mEnv->GetObjectField(mJObject, fieldID);
    Lsp<BWJMethodLocation> result = new BWJMethodLocation(mEnv, obj);
    mEnv->DeleteLocalRef(obj);
    return result;
}

jint BWJTraceMethodInfoBase::getTraceMethodFlags() {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "traceMethodFlags", "I");
    return mEnv->GetIntField(mJObject, fieldID);
}
jint BWJTraceMethodInfoBase::getGranularity() {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "granularity", "I");
    return mEnv->GetIntField(mJObject, fieldID);
}
jint BWJTraceMethodInfoBase::getPromptMethodType() {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "promptMethodType", "I");
    return mEnv->GetIntField(mJObject, fieldID);
}

void BWJTraceMethodInfoBase::setMethodLocation(Lsp<BWJMethodLocation> methodLocation) {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "methodLocation", "Landroid/bw/db/base/MethodLocation;");
    mEnv->SetObjectField(mJObject, fieldID, methodLocation->GetJObject());
}

void BWJTraceMethodInfoBase::setTraceMethodFlags(jint traceMethodFlags) {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "traceMethodFlags", "I");
    mEnv->SetIntField(mJObject, fieldID, traceMethodFlags);
}
void BWJTraceMethodInfoBase::setGranularity(jint granularity) {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "granularity", "I");
    mEnv->SetIntField(mJObject, fieldID, granularity);
}
void BWJTraceMethodInfoBase::setPromptMethodType(jint promptMethodType) {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "promptMethodType", "I");
    mEnv->SetIntField(mJObject, fieldID, promptMethodType);
}

//////////////////////////////////////////////////////////////////////////
// ContentData

Lsp<BWJContentData> BWJContentData::New(JNIEnv* env) {
    jclass clazz = env->FindClass("android/bw/db/base/HookMethodInstInfoBase$ContentData");
    jmethodID methodID = env->GetMethodID(clazz, "<init>", "()V");
    jobject obj = env->NewObject(clazz, methodID);
    Lsp<BWJContentData> result = new BWJContentData(env, obj);
    env->DeleteLocalRef(obj);
    env->DeleteLocalRef(clazz);
    return result;
}

Lsp<BWJContentData> BWJContentData::New(JNIEnv* env, Lsp<HookMethodInstInfoBase::ContentData> nativeContentData) {
    Lsp<BWJContentData> contentData = BWJContentData::New(env);
    contentData->setHookDexPath(nativeContentData->hookDexPath.GetCString());
    contentData->setHookClassDesc(nativeContentData->hookClassDesc.GetCString());
    contentData->setHookMethodName(nativeContentData->hookMethodName.GetCString());
    contentData->setHookMethodSig(nativeContentData->hookMethodSig.GetCString());
    contentData->setIsHookMethodStatic(nativeContentData->isHookMethodStatic);
    contentData->setThisRegister(nativeContentData->thisRegister.GetCString());
    contentData->setParamRegisters(nativeContentData->paramRegisters.GetCString());
    contentData->setReturnRegister(nativeContentData->returnRegister.GetCString());
    return contentData;
}

BWJContentData::BWJContentData(JNIEnv* env, jobject obj) : BWJContentDataSuper(env, obj) {}

BWJContentData::~BWJContentData() {}

CString BWJContentData::getHookDexPath() {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "hookDexPath", "Ljava/lang/String;");
    jstring jStr = (jstring) mEnv->GetObjectField(mJObject, fieldID);
    CString str;
    if (NULL == jStr) {
        return str;
    } else {
        str = JStringToCString(mEnv, jStr);
        mEnv->DeleteLocalRef(jStr);
        return str;
    }
}

CString BWJContentData::getHookClassDesc() {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "hookClassDesc", "Ljava/lang/String;");
    jstring jStr = (jstring) mEnv->GetObjectField(mJObject, fieldID);
    CString str;
    if (NULL == jStr) {
        return str;
    } else {
        str = JStringToCString(mEnv, jStr);
        mEnv->DeleteLocalRef(jStr);
        return str;
    }
}

CString BWJContentData::getHookMethodName() {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "hookMethodName", "Ljava/lang/String;");
    jstring jStr = (jstring) mEnv->GetObjectField(mJObject, fieldID);
    CString str;
    if (NULL == jStr) {
        return str;
    } else {
        str = JStringToCString(mEnv, jStr);
        mEnv->DeleteLocalRef(jStr);
        return str;
    }
}

CString BWJContentData::getHookMethodSig() {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "hookMethodSig", "Ljava/lang/String;");
    jstring jStr = (jstring) mEnv->GetObjectField(mJObject, fieldID);
    CString str;
    if (NULL == jStr) {
        return str;
    } else {
        str = JStringToCString(mEnv, jStr);
        mEnv->DeleteLocalRef(jStr);
        return str;
    }
}

jboolean BWJContentData::isHookMethodStatic() {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "isHookMethodStatic", "Z");
    return mEnv->GetBooleanField(mJObject, fieldID);
}

CString BWJContentData::getThisRegister() {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "thisRegister", "Ljava/lang/String;");
    jstring jStr = (jstring) mEnv->GetObjectField(mJObject, fieldID);
    CString str;
    if (NULL == jStr) {
        return str;
    } else {
        str = JStringToCString(mEnv, jStr);
        mEnv->DeleteLocalRef(jStr);
        return str;
    }
}

CString BWJContentData::getParamRegisters() {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "paramRegisters", "Ljava/lang/String;");
    jstring jStr = (jstring) mEnv->GetObjectField(mJObject, fieldID);
    CString str;
    if (NULL == jStr) {
        return str;
    } else {
        str = JStringToCString(mEnv, jStr);
        mEnv->DeleteLocalRef(jStr);
        return str;
    }
}

CString BWJContentData::getReturnRegister() {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "returnRegister", "Ljava/lang/String;");
    jstring jStr = (jstring) mEnv->GetObjectField(mJObject, fieldID);
    CString str;
    if (NULL == jStr) {
        return str;
    } else {
        str = JStringToCString(mEnv, jStr);
        mEnv->DeleteLocalRef(jStr);
        return str;
    }
}

void BWJContentData::setHookDexPath(const char* hookDexPath) {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "hookDexPath", "Ljava/lang/String;");
    jstring jStr = CStringToJString(mEnv, hookDexPath);
    mEnv->SetObjectField(mJObject, fieldID, jStr);
    mEnv->DeleteLocalRef(jStr);
}

void BWJContentData::setHookClassDesc(const char* hookClassDesc) {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "hookClassDesc", "Ljava/lang/String;");
    jstring jStr = CStringToJString(mEnv, hookClassDesc);
    mEnv->SetObjectField(mJObject, fieldID, jStr);
    mEnv->DeleteLocalRef(jStr);
}

void BWJContentData::setHookMethodName(const char* hookMethodName) {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "hookMethodName", "Ljava/lang/String;");
    jstring jStr = CStringToJString(mEnv, hookMethodName);
    mEnv->SetObjectField(mJObject, fieldID, jStr);
    mEnv->DeleteLocalRef(jStr);
}

void BWJContentData::setHookMethodSig(const char* hookMethodSig) {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "hookMethodSig", "Ljava/lang/String;");
    jstring jStr = CStringToJString(mEnv, hookMethodSig);
    mEnv->SetObjectField(mJObject, fieldID, jStr);
    mEnv->DeleteLocalRef(jStr);
}

void BWJContentData::setIsHookMethodStatic(jboolean isHookMethodStatic) {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "isHookMethodStatic", "Z");
    mEnv->SetBooleanField(mJObject, fieldID, isHookMethodStatic);
}

void BWJContentData::setThisRegister(const char* thisRegister) {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "thisRegister", "Ljava/lang/String;");
    jstring jStr = CStringToJString(mEnv, thisRegister);
    mEnv->SetObjectField(mJObject, fieldID, jStr);
    mEnv->DeleteLocalRef(jStr);
}

void BWJContentData::setParamRegisters(const char* paramRegisters) {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "paramRegisters", "Ljava/lang/String;");
    jstring jStr = CStringToJString(mEnv, paramRegisters);
    mEnv->SetObjectField(mJObject, fieldID, jStr);
    mEnv->DeleteLocalRef(jStr);
}

void BWJContentData::setReturnRegister(const char* returnRegister) {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "returnRegister", "Ljava/lang/String;");
    jstring jStr = CStringToJString(mEnv, returnRegister);
    mEnv->SetObjectField(mJObject, fieldID, jStr);
    mEnv->DeleteLocalRef(jStr);
}

//////////////////////////////////////////////////////////////////////////
// HookMethodInstInfoBase

Lsp<BWJHookMethodInstInfoBase> BWJHookMethodInstInfoBase::New(JNIEnv* env) {
    jclass clazz = env->FindClass("android/bw/db/base/HookMethodInstInfoBase");
    jmethodID methodID = env->GetMethodID(clazz, "<init>", "()V");
    jobject obj = env->NewObject(clazz, methodID);
    Lsp<BWJHookMethodInstInfoBase> result = new BWJHookMethodInstInfoBase(env, obj);
    env->DeleteLocalRef(obj);
    env->DeleteLocalRef(clazz);
    return result;
}

BWJHookMethodInstInfoBase::BWJHookMethodInstInfoBase(JNIEnv* env, jobject obj)
    : BWJHookMethodInstInfoBaseSuper(env, obj) {}

BWJHookMethodInstInfoBase::~BWJHookMethodInstInfoBase() {}

Lsp<BWJInstructionLocation> BWJHookMethodInstInfoBase::getInstructionLocation() {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "instructionLocation", "Landroid/bw/db/base/InstructionLocation;");
    jobject obj = mEnv->GetObjectField(mJObject, fieldID);
    Lsp<BWJInstructionLocation> result = new BWJInstructionLocation(mEnv, obj);
    mEnv->DeleteLocalRef(obj);
    return result;
}

Lsp<BWJContentData> BWJHookMethodInstInfoBase::getContentData() {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "contentData", "Landroid/bw/db/base/HookMethodInstInfoBase$ContentData;");
    jobject obj = mEnv->GetObjectField(mJObject, fieldID);
    Lsp<BWJContentData> result = new BWJContentData(mEnv, obj);
    mEnv->DeleteLocalRef(obj);
    return result;
}

void BWJHookMethodInstInfoBase::setInstructionLocation(Lsp<BWJInstructionLocation> instructionLocation) {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "instructionLocation", "Landroid/bw/db/base/InstructionLocation;");
    mEnv->SetObjectField(mJObject, fieldID, instructionLocation->GetJObject());
}

void BWJHookMethodInstInfoBase::setContentData(Lsp<BWJContentData> contentData) {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "contentData", "Landroid/bw/db/base/HookMethodInstInfoBase$ContentData;");
    mEnv->SetObjectField(mJObject, fieldID, contentData->GetJObject());
}

//////////////////////////////////////////////////////////////////////////
// BWDumpBase

BWJBWDumpBase::BWJBWDumpBase(JNIEnv* env, jobject obj) : BWJBWDumpBaseSuper(env, obj) {}

BWJBWDumpBase::~BWJBWDumpBase() {}

Lsp<BWJAppIDBase> BWJBWDumpBase::getAppIDBase() {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "appIDBase", "Landroid/bw/db/base/BWDumpBase;");
    jobject obj = mEnv->GetObjectField(mJObject, fieldID);
    Lsp<BWJAppIDBase> result = new BWJAppIDBase(mEnv, obj);
    mEnv->DeleteLocalRef(obj);
    return result;
}

jint BWJBWDumpBase::getBwDumpFlags() {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "bwDumpFlags", "I");
    return mEnv->GetIntField(mJObject, fieldID);
}

void BWJBWDumpBase::setAppIDBase(BWJAppIDBase* appIDBase) {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "appIDBase", "Landroid/bw/db/base/BWDumpBase;");
    mEnv->SetObjectField(mJObject, fieldID, appIDBase->GetJObject());
}

void BWJBWDumpBase::setBwDumpFlags(jint bwDumpFlags) {
    jfieldID fieldID = mEnv->GetFieldID(mClass, "bwDumpFlags", "I");
    mEnv->SetIntField(mJObject, fieldID, bwDumpFlags);
}
