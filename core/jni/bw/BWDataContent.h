#pragma once

#include <SmartPointer.h>
#include <BWNativeHelper/BWCommon.h>

#include "BWJavaBase.h"

//////////////////////////////////////////////////////////////////////////
// AppIDBase

#define BWJObjectSuper BWJObject

class BWJAppIDBase : public BWJObjectSuper {
public:
    static Lsp<BWJAppIDBase> New(JNIEnv* env, jint uid, CString packageName, jint appType, jint startFlags);

    static Lsp<BWJAppIDBase> New(JNIEnv* env, Lsp<AppIDBase> nativeAppIDBase);

    BWJAppIDBase(JNIEnv* env, jobject obj);

    virtual ~BWJAppIDBase();

    jint getUid();

    void setUid(jint uid);

    CString getPackageName();

    void setPackageName(CString packageName);

    jint getAppType();

    void setAppType(jint appType);

    jint getStartFlags();

    void setStartFlags(jint startFlags);
};

//////////////////////////////////////////////////////////////////////////
// MethodIDBase

#define BWJMethodIDBaseSuper BWJObject

class BWJMethodIDBase : public BWJMethodIDBaseSuper {
public:
    static Lsp<BWJMethodIDBase> New(JNIEnv* env, CString classDesc, CString methodName, CString methodSig);

    static Lsp<BWJMethodIDBase> New(JNIEnv* env, Lsp<MethodIDBase> nativeMethodIDBase);

    BWJMethodIDBase(JNIEnv* env, jobject obj);
    virtual ~BWJMethodIDBase();

    CString getClassDesc();

    CString getMethodName();

    CString getMethodSig();

    jint getHash();

    void setClassDesc(CString classDesc);

    void setMethodName(CString methodName);

    void setMethodSig(CString methodSig);
};

//////////////////////////////////////////////////////////////////////////
// MethodLocation

#define BWJMethodLocationSuper BWJObject

class BWJMethodLocation : public BWJMethodLocationSuper {
public:
    static Lsp<BWJMethodLocation> New(JNIEnv* env, Lsp<BWJAppIDBase> appIDBase, Lsp<BWJMethodIDBase> methodIDBase);

    static Lsp<BWJMethodLocation> New(JNIEnv* env, Lsp<AppIDBase> nativeAppIDBase, Lsp<MethodIDBase> nativeMethodIDBase);

    BWJMethodLocation(JNIEnv* env, jobject obj);
    virtual ~BWJMethodLocation();

    Lsp<BWJAppIDBase> getAppIDBase();
    Lsp<BWJMethodIDBase> getMethodIDBase();
    void setAppIDBase(BWJAppIDBase* appIDBase);
    void setMethodIDBase(BWJMethodIDBase* methodIDBase);
};

//////////////////////////////////////////////////////////////////////////
// InstructionLocation

#define BWJInstructionLocationSuper BWJMethodLocation

class BWJInstructionLocation : public BWJInstructionLocationSuper {
public:
    static Lsp<BWJInstructionLocation> New(JNIEnv* env, Lsp<BWJAppIDBase> appIDBase, 
        Lsp<BWJMethodIDBase> methodIDBase, int64_t instLineNum, int64_t dexPC);

    static Lsp<BWJInstructionLocation> New(JNIEnv* env, Lsp<AppIDBase> nativeAppIDBase, 
        Lsp<MethodIDBase> nativeMethodIDBase, int64_t instLineNum, int64_t dexPC);

    static Lsp<BWJInstructionLocation> New(JNIEnv* env, Lsp<InstructionLocation> nativeInstructionLocation);

    BWJInstructionLocation(JNIEnv* env, jobject obj);
    virtual ~BWJInstructionLocation();

    int64_t getInstLineNum();
    int64_t getDexPC();
    void setInstLineNum(int64_t instLineNum);
    void setDexPC(int64_t dexPC);
};

//////////////////////////////////////////////////////////////////////////
// TraceMethodInfoBase

#define BWJTraceMethodInfoBaseSuper BWJObject

class BWJTraceMethodInfoBase : public BWJTraceMethodInfoBaseSuper {
public:
    BWJTraceMethodInfoBase(JNIEnv* env, jobject obj);
    virtual ~BWJTraceMethodInfoBase();

    static Lsp<BWJTraceMethodInfoBase> New(JNIEnv* env);

    Lsp<BWJMethodLocation> getMethodLocation();
    jint getTraceMethodFlags();
    jint getGranularity();
    jint getPromptMethodType();

    void setMethodLocation(Lsp<BWJMethodLocation> methodLocation);
    void setTraceMethodFlags(jint flags);
    void setGranularity(jint granularity);
    void setPromptMethodType(jint promptMethodType);
};

//////////////////////////////////////////////////////////////////////////
// ContentData

#define BWJContentDataSuper BWJObject

class BWJContentData : public BWJContentDataSuper {
public:
    static Lsp<BWJContentData> New(JNIEnv* env);

    static Lsp<BWJContentData> New(JNIEnv* env, Lsp<HookMethodInstInfoBase::ContentData> nativeContentData);

    BWJContentData(JNIEnv* env, jobject obj);
    virtual ~BWJContentData();

    CString getHookDexPath();
    CString getHookClassDesc();
    CString getHookMethodName();
    CString getHookMethodSig();
    jboolean isHookMethodStatic();
    CString getThisRegister();
    CString getParamRegisters();
    CString getReturnRegister();

    void setHookDexPath(const char* hookDexPath);

    void setHookClassDesc(const char* hookClassDesc);

    void setHookMethodName(const char* hookMethodName);

    void setHookMethodSig(const char* hookMethodSig);

    void setIsHookMethodStatic(jboolean isHookMethodStatic);

    void setThisRegister(const char* thisRegister);

    void setParamRegisters(const char* paramRegisters);

    void setReturnRegister(const char* returnRegister);
};

//////////////////////////////////////////////////////////////////////////
// HookMethodInstInfoBase

#define BWJHookMethodInstInfoBaseSuper BWJObject

class BWJHookMethodInstInfoBase : public BWJHookMethodInstInfoBaseSuper {
public:
    static Lsp<BWJHookMethodInstInfoBase> New(JNIEnv* env);
    BWJHookMethodInstInfoBase(JNIEnv* env, jobject obj);
    virtual ~BWJHookMethodInstInfoBase();

    Lsp<BWJInstructionLocation> getInstructionLocation();
    Lsp<BWJContentData> getContentData();

    void setInstructionLocation(Lsp<BWJInstructionLocation> instructionLocation);
    void setContentData(Lsp<BWJContentData> contentData);
};

//////////////////////////////////////////////////////////////////////////
// BWDumpBase

#define BWJBWDumpBaseSuper BWJObject

class BWJBWDumpBase : public BWJBWDumpBaseSuper {
public:
    BWJBWDumpBase(JNIEnv* env, jobject obj);
    virtual ~BWJBWDumpBase();

    Lsp<BWJAppIDBase> getAppIDBase();
    jint getBwDumpFlags();

    void setAppIDBase(BWJAppIDBase* appIDBase);
    void setBwDumpFlags(jint flags);
};
