#include "stdafx.h"
#include "BWJniHelper.h"
#include "BWDataContent.h"
#include <BWNativeHelper/BWNativeHelper.h>
#include "BWJavaContainter.h"

extern "C" jboolean Java_android_bw_socket_BWAppAnalysisControlServer_nativeInsertOrUpdateTraceMethodInfo(
    JNIEnv* env, jobject thiz, jobject traceMethodInfoBase) {
    return nativeInsertOrUpdateTraceMethodInfo(env, traceMethodInfoBase);
}

extern "C" jboolean Java_android_bw_socket_BWAppAnalysisControlServer_nativeDeleteTraceMethodInfo(JNIEnv* env, jobject thiz, jint hash) {
    return BWNativeHelper::DeleteTraceMethodInfo(hash);
}

extern "C" jobject Java_android_bw_socket_BWAppAnalysisControlServer_nativeQueryTraceMethodInfo(JNIEnv* env, jobject thiz, jint hash) {
    Lsp<TraceMethodInfoBase> nativeTraceMethodInfoBase;
    // BWLOGI("[*] %s - hash=%d", __FUNCTION__, hash);
    if (!BWNativeHelper::QueryTraceMethodInfo(hash, &nativeTraceMethodInfoBase)) {
        BWLOGI("[-] Java_android_bw_socket_BWAppAnalysisControlServer_nativeQueryTraceMethodInfo - "
            "BWNativeHelper::QueryTraceMethodInfo失败。");
        return NULL;
    }

    Lsp<MethodLocation> nativeMethodLocation = nativeTraceMethodInfoBase->methodLocation;
    if (nativeMethodLocation.IsEmpty()) {
        BWLOGI("[-] Java_android_bw_socket_BWAppAnalysisControlServer_nativeQueryTraceMethodInfo - "
            "nativeMethodLocation为空。");
        return NULL;
    }
    if (nativeMethodLocation->appIDBase.IsEmpty()) {
        BWLOGI("[-] Java_android_bw_socket_BWAppAnalysisControlServer_nativeQueryTraceMethodInfo - "
            "nativeMethodLocation->appIDBase为空。");
        return NULL;
    }
    if (nativeMethodLocation->methodIDBase.IsEmpty()) {
        BWLOGI("[-] Java_android_bw_socket_BWAppAnalysisControlServer_nativeQueryTraceMethodInfo - "
            "nativeMethodLocation->methodIDBase为空。");
        return NULL;
    }
    Lsp<BWJMethodLocation> methodLocation = BWJMethodLocation::New(env, 
        nativeMethodLocation->appIDBase, nativeMethodLocation->methodIDBase);

    Lsp<BWJTraceMethodInfoBase> jTraceMethodInfoBase = BWJTraceMethodInfoBase::New(env);
    jTraceMethodInfoBase->setMethodLocation(methodLocation);
    jTraceMethodInfoBase->setTraceMethodFlags(nativeTraceMethodInfoBase->traceMethodFlags);
    jTraceMethodInfoBase->setGranularity((int)(nativeTraceMethodInfoBase->granularity));
    jTraceMethodInfoBase->setPromptMethodType((int)(nativeTraceMethodInfoBase->promptMethodType));

    return env->NewLocalRef(jTraceMethodInfoBase->GetJObject());
}

extern "C" jboolean Java_android_bw_socket_BWAppAnalysisControlServer_nativeInsertOrUpdateHookMethodInstInfo(
    JNIEnv* env, jobject thiz, jobject hookMethodInstInfo) {
    return nativeInsertOrUpdateHookMethodInstInfo(env, hookMethodInstInfo);
}

extern "C" jint Java_android_bw_socket_BWAppAnalysisControlServer_nativeDeleteHookMethodInstInfo(
    JNIEnv* env, jobject thiz, jint hash, jlong instLineNum) {
    return BWNativeHelper::DeleteHookMethodInstInfo(hash, instLineNum);
}

extern "C" jint Java_android_bw_socket_BWAppAnalysisControlServer_nativeDeleteHookMethodInstInfoInMethod(int hash) {
    return BWNativeHelper::DeleteHookMethodInstInfoInMethod(hash);
}

extern "C" jint Java_android_bw_socket_BWAppAnalysisControlServer_nativeDeleteHookMethodInstInfoInPackage() {
    return BWNativeHelper::DeleteHookMethodInstInfoInPackage();
}

extern "C" jobject Java_android_bw_socket_BWAppAnalysisControlServer_nativeQueryHookMethodInstInfoInMethod(
    JNIEnv* env, jobject thiz, jint hash) {

    std::vector< Lsp<HookMethodInstInfoBase> > hookMethodInstInfos;
    if (!BWNativeHelper::QueryHookMethodInstInfoInMethod(hash, &hookMethodInstInfos)) {
        return NULL;
    }
    size_t arraySize = hookMethodInstInfos.size();
    if (0 == arraySize) {
        return NULL;
    }

    Lsp< BWJArrayList<jobject> > list = BWJArrayList<jobject>::New(env);

    for (size_t i = 0; i < arraySize; i++) {
        Lsp<HookMethodInstInfoBase> info = hookMethodInstInfos[i];

        Lsp<BWJHookMethodInstInfoBase> o = BWJHookMethodInstInfoBase::New(env);
        Lsp<BWJInstructionLocation> instructionLocation = BWJInstructionLocation::New(
            env, info->instructionLocation);
        o->setInstructionLocation(instructionLocation);

        Lsp<BWJContentData> contentData = BWJContentData::New(env, info->contentData);
        o->setContentData(contentData);
        list->add(o->GetJObject());
    }

    return list->GetJObject();
}

extern "C" jobject Java_android_bw_socket_BWAppAnalysisControlServer_nativeQueryHookMethodInstInfo(
    JNIEnv* env, jobject thiz, int hash, long instLineNum) {
    Lsp<HookMethodInstInfoBase> info;
    if (!BWNativeHelper::QueryHookMethodInstInfo(hash, instLineNum, &info)) {
        return NULL;
    }
    
    Lsp<BWJHookMethodInstInfoBase> o = BWJHookMethodInstInfoBase::New(env);
    Lsp<BWJInstructionLocation> instructionLocation = BWJInstructionLocation::New(
        env, info->instructionLocation);
    o->setInstructionLocation(instructionLocation);
    Lsp<BWJContentData> contentData = BWJContentData::New(env, info->contentData);
    o->setContentData(contentData);
    return o->GetJObject();
}

extern "C" jboolean Java_android_bw_socket_BWAppAnalysisControlServer_nativeSetBWDumpFlags(JNIEnv* env, jobject thiz, jint flags) {
    return BWNativeHelper::SetBWDumpFlags(flags);
}

extern "C" jint Java_android_bw_socket_BWAppAnalysisControlServer_nativeGetBWDumpFlags(JNIEnv* env, jobject thiz) {
    return BWNativeHelper::GetBWDumpFlags();
}
