#include "stdafx.h"
#include <BWNativeHelper/BWNativeHelper.h>
#include "BWJniHelper.h"
#include "BWDataContent.h"

#ifndef LIKELY
#define LIKELY(x)       __builtin_expect((x), true)
#endif

#ifndef LIKELY
#define UNLIKELY(x)     __builtin_expect((x), false)
#endif

/*
 * Retrieve the next UTF-16 character from a UTF-8 string.
 *
 * Advances "*utf8_data_in" to the start of the next character.
 *
 * WARNING: If a string is corrupted by dropping a '\0' in the middle
 * of a 3-byte sequence, you can end up overrunning the buffer with
 * reads (and possibly with the writes if the length was computed and
 * cached before the damage). For performance reasons, this function
 * assumes that the string being parsed is known to be valid (e.g., by
 * already being verified). Most strings we process here are coming
 * out of dex files or other internal translations, so the only real
 * risk comes from the JNI NewStringUTF call.
 */
inline uint16_t GetUtf16FromUtf8(const char** utf8_data_in) {
  uint8_t one = *(*utf8_data_in)++;
  if ((one & 0x80) == 0) {
    // one-byte encoding
    return one;
  }
  // two- or three-byte encoding
  uint8_t two = *(*utf8_data_in)++;
  if ((one & 0x20) == 0) {
    // two-byte encoding
    return ((one & 0x1f) << 6) | (two & 0x3f);
  }
  // three-byte encoding
  uint8_t three = *(*utf8_data_in)++;
  return ((one & 0x0f) << 12) | ((two & 0x3f) << 6) | (three & 0x3f);
}

// Helper for IsValidPartOfMemberNameUtf8(), a bit vector indicating valid low ascii.
uint32_t DEX_MEMBER_VALID_LOW_ASCII[4] = {
  0x00000000,  // 00..1f low control characters; nothing valid
  0x03ff2010,  // 20..3f digits and symbols; valid: '0'..'9', '$', '-'
  0x87fffffe,  // 40..5f uppercase etc.; valid: 'A'..'Z', '_'
  0x07fffffe   // 60..7f lowercase etc.; valid: 'a'..'z'
};

// Helper for IsValidPartOfMemberNameUtf8(); do not call directly.
bool IsValidPartOfMemberNameUtf8Slow(const char** pUtf8Ptr) {
  /*
   * It's a multibyte encoded character. Decode it and analyze. We
   * accept anything that isn't (a) an improperly encoded low value,
   * (b) an improper surrogate pair, (c) an encoded '\0', (d) a high
   * control character, or (e) a high space, layout, or special
   * character (U+00a0, U+2000..U+200f, U+2028..U+202f,
   * U+fff0..U+ffff). This is all specified in the dex format
   * document.
   */

  uint16_t utf16 = GetUtf16FromUtf8(pUtf8Ptr);

  // Perform follow-up tests based on the high 8 bits.
  switch (utf16 >> 8) {
  case 0x00:
    // It's only valid if it's above the ISO-8859-1 high space (0xa0).
    return (utf16 > 0x00a0);
  case 0xd8:
  case 0xd9:
  case 0xda:
  case 0xdb:
    // It's a leading surrogate. Check to see that a trailing
    // surrogate follows.
    utf16 = GetUtf16FromUtf8(pUtf8Ptr);
    return (utf16 >= 0xdc00) && (utf16 <= 0xdfff);
  case 0xdc:
  case 0xdd:
  case 0xde:
  case 0xdf:
    // It's a trailing surrogate, which is not valid at this point.
    return false;
  case 0x20:
  case 0xff:
    // It's in the range that has spaces, controls, and specials.
    switch (utf16 & 0xfff8) {
    case 0x2000:
    case 0x2008:
    case 0x2028:
    case 0xfff0:
    case 0xfff8:
      return false;
    }
    break;
  }
  return true;
}

/* Return whether the pointed-at modified-UTF-8 encoded character is
 * valid as part of a member name, updating the pointer to point past
 * the consumed character. This will consume two encoded UTF-16 code
 * points if the character is encoded as a surrogate pair. Also, if
 * this function returns false, then the given pointer may only have
 * been partially advanced.
 */
static bool IsValidPartOfMemberNameUtf8(const char** pUtf8Ptr) {
  uint8_t c = (uint8_t) **pUtf8Ptr;
  if (LIKELY(c <= 0x7f)) {
    // It's low-ascii, so check the table.
    uint32_t wordIdx = c >> 5;
    uint32_t bitIdx = c & 0x1f;
    (*pUtf8Ptr)++;
    return (DEX_MEMBER_VALID_LOW_ASCII[wordIdx] & (1 << bitIdx)) != 0;
  }

  // It's a multibyte encoded character. Call a non-inline function
  // for the heavy lifting.
  return IsValidPartOfMemberNameUtf8Slow(pUtf8Ptr);
}

enum ClassNameType { kName, kDescriptor };
static bool IsValidClassName(const char* s, ClassNameType type, char separator) {
  int arrayCount = 0;
  while (*s == '[') {
    arrayCount++;
    s++;
  }

  if (arrayCount > 255) {
    // Arrays may have no more than 255 dimensions.
    return false;
  }

  if (arrayCount != 0) {
    /*
     * If we're looking at an array of some sort, then it doesn't
     * matter if what is being asked for is a class name; the
     * format looks the same as a type descriptor in that case, so
     * treat it as such.
     */
    type = kDescriptor;
  }

  if (type == kDescriptor) {
    /*
     * We are looking for a descriptor. Either validate it as a
     * single-character primitive type, or continue on to check the
     * embedded class name (bracketed by "L" and ";").
     */
    switch (*(s++)) {
    case 'B':
    case 'C':
    case 'D':
    case 'F':
    case 'I':
    case 'J':
    case 'S':
    case 'Z':
      // These are all single-character descriptors for primitive types.
      return (*s == '\0');
    case 'V':
      // Non-array void is valid, but you can't have an array of void.
      return (arrayCount == 0) && (*s == '\0');
    case 'L':
      // Class name: Break out and continue below.
      break;
    default:
      // Oddball descriptor character.
      return false;
    }
  }

  /*
   * We just consumed the 'L' that introduces a class name as part
   * of a type descriptor, or we are looking for an unadorned class
   * name.
   */

  bool sepOrFirst = true;  // first character or just encountered a separator.
  for (;;) {
    uint8_t c = (uint8_t) *s;
    switch (c) {
    case '\0':
      /*
       * Premature end for a type descriptor, but valid for
       * a class name as long as we haven't encountered an
       * empty component (including the degenerate case of
       * the empty string "").
       */
      return (type == kName) && !sepOrFirst;
    case ';':
      /*
       * Invalid character for a class name, but the
       * legitimate end of a type descriptor. In the latter
       * case, make sure that this is the end of the string
       * and that it doesn't end with an empty component
       * (including the degenerate case of "L;").
       */
      return (type == kDescriptor) && !sepOrFirst && (s[1] == '\0');
    case '/':
    case '.':
      if (c != separator) {
        // The wrong separator character.
        return false;
      }
      if (sepOrFirst) {
        // Separator at start or two separators in a row.
        return false;
      }
      sepOrFirst = true;
      s++;
      break;
    default:
      if (!IsValidPartOfMemberNameUtf8(&s)) {
        return false;
      }
      sepOrFirst = false;
      break;
    }
  }
}

bool IsValidBinaryClassNameBW(const char* s) {
  return IsValidClassName(s, kName, '.');
}

bool IsValidJniClassNameBW(const char* s) {
  return IsValidClassName(s, kName, '/');
}

bool IsValidDescriptorBW(const char* s) {
  return IsValidClassName(s, kDescriptor, '/');
}

bool IsMethodExist(JNIEnv* env, const char* jniClassName, const char* methodName, const char* methodSig) {
    if ((NULL == jniClassName) || !IsValidJniClassNameBW(jniClassName)) {
        BWLOGE("[-] IsMethodExist - 非法类名，合法的类名应该是\"包名/类\"。例如：java/lang/String，[Ljava/lang/String;，[[B。");
        return false;
    }
    jclass clazz = NULL;
    clazz = env->FindClass(jniClassName);
    if (env->ExceptionCheck()) {
        BWLOGE("[-] IsMethodExist - 类名：%s。", jniClassName);
        env->ExceptionDescribe();
        env->ExceptionClear();
        return false;
    }
    if (NULL == clazz) {
        BWLOGE("[-] IsMethodExist - 获得类失败却没有抛出异常，不科学！");
        return false;
    }

    bool result = false;
    do {
        jmethodID methodID = env->GetMethodID(clazz, methodName, methodSig);
        if (env->ExceptionCheck()) {
            env->ExceptionDescribe();
            env->ExceptionClear();
            break;
        }
        if (NULL == methodID) {
            BWLOGE("[-] IsMethodExist - 获得methodID失败却没有抛出异常，这不科学！");
            break;
        }
        result = true;
    } while (false);
    if (NULL != clazz) {
        env->DeleteLocalRef(clazz);
    }

    return result;
}

bool ClassDescriptorToJniClassName(const char* classDesc, char** jniClassName) {
    size_t classDescLen = strlen(classDesc);
    if (classDescLen < 3) {
        BWLOGE("[-] ClassDescriptorToJniClassName - 类描述符长度过小，长度=%zu。", classDescLen);
        return false;
    }
    if (('L' != classDesc[0]) || (';' != classDesc[classDescLen - 1])) {
        BWLOGE("[-] ClassDescriptorToJniClassName - 类描述符不是以'L'起始或不是以';'结尾。类描述符：%s。", classDesc);
        return false;
    }
    size_t jniClassNameLen = classDescLen - 2;
    *jniClassName = (char*) malloc(jniClassNameLen + 1);
    if (NULL == *jniClassName) {
        BWLOGE("[-] ClassDescriptorToJniClassName - 分配内存失败。");
        return false;
    }
    memcpy(*jniClassName, classDesc + 1, jniClassNameLen);
    (*jniClassName)[jniClassNameLen] = '\0';
    return true;
}

jboolean nativeInsertOrUpdateTraceMethodInfo(JNIEnv* env, jobject traceMethodInfoBase) {
    // 不验证方法是否存在，因为在当前的JNIEnv*所对应的ClassLoader中可能没有这个方法，而这个方法可能存在于其他的ClassLoader中。

    // BWLOGI("[*] nativeInsertOrUpdateTraceMethodInfo - entry");
    BWJTraceMethodInfoBase jJTraceMethodInfoBase(env, traceMethodInfoBase);
    // BWLOGI("[*] nativeInsertOrUpdateTraceMethodInfo - 1");
    Lsp<BWJMethodLocation> methodLocation = jJTraceMethodInfoBase.getMethodLocation();
    // BWLOGI("[*] nativeInsertOrUpdateTraceMethodInfo - 2");
    Lsp<BWJAppIDBase> appIDBase = methodLocation->getAppIDBase();
    // BWLOGI("[*] nativeInsertOrUpdateTraceMethodInfo - 3");
    Lsp<BWJMethodIDBase> methodIDBase = methodLocation->getMethodIDBase();
    // BWLOGI("[*] nativeInsertOrUpdateTraceMethodInfo - 4");
    

    // int uid = appIDBase->getUid();
    // BWLOGI("[*] nativeInsertOrUpdateTraceMethodInfo - uid=%d", uid);
    // CString packageName = appIDBase->getPackageName();
    // BWLOGI("[*] nativeInsertOrUpdateTraceMethodInfo - packageName=%s", packageName.GetCString());
    // int appType = appIDBase->getAppType();
    // BWLOGI("[*] nativeInsertOrUpdateTraceMethodInfo - appType=%d", appType);
    // int startFlags = appIDBase->getStartFlags();
    // BWLOGI("[*] nativeInsertOrUpdateTraceMethodInfo - startFlags=%d", startFlags);
    // Lsp<AppIDBase> nativeAppIDBase = new AppIDBase(uid, packageName.GetCString(),
    //     appType, startFlags);
    Lsp<AppIDBase> nativeAppIDBase = new AppIDBase(appIDBase->getUid(),
        appIDBase->getPackageName().GetCString(), appIDBase->getAppType(), appIDBase->getStartFlags());
    // int hash = methodIDBase->getHash();
    // BWLOGI("[*] nativeInsertOrUpdateTraceMethodInfo - 5, hash=%d", hash);
    Lsp<MethodIDBase> nativeMethodIDBase = new MethodIDBase(methodIDBase->getClassDesc().GetCString(),
        methodIDBase->getMethodName().GetCString(), methodIDBase->getMethodSig().GetCString(),
        methodIDBase->getHash());
    // BWLOGI("[*] nativeInsertOrUpdateTraceMethodInfo - 6");

    Lsp<TraceMethodInfoBase> nativeTraceMethodInfoBase = new TraceMethodInfoBase();
    // BWLOGI("[*] nativeInsertOrUpdateTraceMethodInfo - 7");
    nativeTraceMethodInfoBase->methodLocation = new MethodLocation(nativeAppIDBase, nativeMethodIDBase);
    // BWLOGI("[*] nativeInsertOrUpdateTraceMethodInfo - 8");
    nativeTraceMethodInfoBase->traceMethodFlags = jJTraceMethodInfoBase.getTraceMethodFlags();
    // BWLOGI("[*] nativeInsertOrUpdateTraceMethodInfo - 9");
    nativeTraceMethodInfoBase->granularity = (GranularityLevel) jJTraceMethodInfoBase.getGranularity();
    // BWLOGI("[*] nativeInsertOrUpdateTraceMethodInfo - 10");
    nativeTraceMethodInfoBase->promptMethodType = (PromptMethodType) jJTraceMethodInfoBase.getPromptMethodType();
    // BWLOGI("[*] nativeInsertOrUpdateTraceMethodInfo - 11");
    nativeTraceMethodInfoBase->deep = 0;

    // BWLOGI("[*] nativeInsertOrUpdateTraceMethodInfo - 12, hash=%d",
    //   nativeTraceMethodInfoBase->methodLocation->methodIDBase->hash);
    return BWNativeHelper::InsertOrUpdateTraceMethodInfo(&nativeTraceMethodInfoBase);
}

jboolean nativeInsertOrUpdateHookMethodInstInfo(JNIEnv* env, jobject hookMethodInstInfo) {
    BWJHookMethodInstInfoBase jJHookMethodInstInfoBase(env, hookMethodInstInfo);
    Lsp<BWJInstructionLocation> instructionLocation = jJHookMethodInstInfoBase.getInstructionLocation();
    Lsp<BWJAppIDBase> appIDBase = instructionLocation->getAppIDBase();
    Lsp<BWJMethodIDBase> methodIDBase = instructionLocation->getMethodIDBase();
    Lsp<BWJContentData> contentData = jJHookMethodInstInfoBase.getContentData();

    Lsp<AppIDBase> nativeAppIDBase = new AppIDBase(appIDBase->getUid(),
      appIDBase->getPackageName().GetCString(), appIDBase->getAppType(), appIDBase->getStartFlags());
    Lsp<MethodIDBase> nativeMethodIDBase = new MethodIDBase(
      methodIDBase->getClassDesc().GetCString(), methodIDBase->getMethodName().GetCString(),
      methodIDBase->getMethodSig().GetCString(), methodIDBase->getHash());

    Lsp<HookMethodInstInfoBase> nativeHookMethodInstInfoBase = new HookMethodInstInfoBase();

    nativeHookMethodInstInfoBase->instructionLocation = new InstructionLocation(nativeAppIDBase, nativeMethodIDBase,
        instructionLocation->getInstLineNum(), instructionLocation->getDexPC());

    Lsp<HookMethodInstInfoBase::ContentData> nativeContentData = new HookMethodInstInfoBase::ContentData();

    nativeContentData->hookDexPath = contentData->getHookDexPath();
    nativeContentData->hookClassDesc = contentData->getHookClassDesc();
    nativeContentData->hookMethodName = contentData->getHookMethodName();
    nativeContentData->hookMethodSig = contentData->getHookMethodSig();
    nativeContentData->isHookMethodStatic = contentData->isHookMethodStatic();

    nativeContentData->thisRegister = contentData->getThisRegister();
    nativeContentData->paramRegisters = contentData->getParamRegisters();
    nativeContentData->returnRegister = contentData->getReturnRegister();

    nativeHookMethodInstInfoBase->contentData = nativeContentData;

    return BWNativeHelper::InsertOrUpdateHookMethodInstInfo(&nativeHookMethodInstInfoBase);
}
