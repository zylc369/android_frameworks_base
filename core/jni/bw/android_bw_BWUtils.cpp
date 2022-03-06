#include "stdafx.h"
#include "BWJniHelper.h"

extern "C" jboolean Java_android_bw_BWUtils_nativeInsertOrUpdateTraceMethodInfo(JNIEnv* env, jobject thiz, jobject traceMethodInfoBase) {
    return nativeInsertOrUpdateTraceMethodInfo(env, traceMethodInfoBase);
}

extern "C" jstring Java_android_bw_BWUtils_nativeGetProcessName(JNIEnv* env, jobject thiz, jint pid) {
	if (BWDEBUG) {
    	BWLOGI("[*] nativeGetProcessName - pid=%d。", pid);
	}
    char processName[PATH_MAX];
    if (!GetProcessName(pid, processName)) {
        BWLOGE("[-] nativeGetProcessName - GetProcessName error.pid=%d。", pid);
        return NULL;
    }
    return CStringToJString(env, processName);
}
