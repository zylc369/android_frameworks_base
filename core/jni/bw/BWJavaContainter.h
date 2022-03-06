#pragma once

#include "BWJavaBase.h"

//////////////////////////////////////////////////////////////////////////
// List

#define BWJListSuper BWJObject

/**
 * 类型模板T必须是封装的JXXX类型。
 * 如：JString、JClass等。
 */
template<typename T>
class BWJList : public BWJListSuper {
public:
    void add(jint location, T& object) {
        return CallVoidMethod("add", "(ILjava/lang/Object;)V", location, object.GetJObject());
    }

    jboolean add(T& object) {
        return CallBooleanMethod("add", "(Ljava/lang/Object;)Z", object.GetJObject());
    }
    
    jboolean add(jobject object) {
        return CallBooleanMethod("add", "(Ljava/lang/Object;)Z", object);
    }
    
    jboolean add(Lsp<T> object) {
        return CallBooleanMethod("add", "(Ljava/lang/Object;)Z", object->GetJObject());
    }

    void clear() {
        return CallVoidMethod("clear", "()V");
    }

    jboolean contains(BWJObject& object) {
        return CallBooleanMethod("contains", "(Ljava/lang/Object;)Z", object.GetJObject());
    }
    
    jboolean contains(Lsp<T> object) {
        return CallBooleanMethod("contains", "(Ljava/lang/Object;)Z", object->GetJObject());
    }

    /**
     * 获得列表中的元素。
     * @param[in] location 位置。
     * @return 返回列表中的元素，此元素使用完成后需要释放内存。
     */
    Lsp<T> get(jint location) {
        return CallObjectMethod<T>("get", "(I)Ljava/lang/Object;", location);
    }

    jint indexOf(BWJObject& object) {
        return CallIntMethod("indexOf", "(Ljava/lang/Object;)I", object.GetJObject());
    }

    jboolean isEmpty() {
        return CallBooleanMethod("isEmpty", "()Z");
    }

    /**
     * 删除列表中的元素。
     * @param[in] location 位置。
     * @return 返回删除的元素，此元素使用完成后需要释放内存。
     */
    Lsp<T> remove(jint location) {
        return CallObjectMethod<T>("remove", "(I)Ljava/lang/Object;", location);
    }

    jboolean remove(BWJObject& object) {
        return CallBooleanMethod("remove", "(Ljava/lang/Object;)Z", object.GetJObject());
    }
    
    jboolean remove(Lsp<T> object) {
        return CallBooleanMethod("remove", "(Ljava/lang/Object;)Z", object->GetJObject());
    }

    /**
     * @return 返回的对象需要释放内存。
     */
    Lsp<T> set(jint location, T& object) {
        return CallObjectMethod<T>("set", "(ILjava/lang/Object;)Ljava/lang/Object;",
                                   location, object.GetJObject());
    }

    jint size() {
        return CallIntMethod("size", "()I");
    }
    
    jarray toArray() {
        return (jarray)CallObjectMethod(mEnv, mJObject, "toArray", "()[Ljava/lang/Object;");
    }

protected:
    DEFINE_JAVA_CLASS(BWJList);
};

//////////////////////////////////////////////////////////////////////////
// ArrayList

#define BWJArrayListSuper BWJList<T>

template<typename T>
class BWJArrayList : public BWJArrayListSuper {
public:
    DEFINE_JAVA_CLASS(BWJArrayList);

    static Lsp< BWJArrayList<T> > New(JNIEnv* env) {
        return BWJObject::New< BWJArrayList<T> >(env, "java/util/ArrayList");
    }
};
