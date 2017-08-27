#include "main.h"
#include <jni.h>

#define JNIREG_CLASS "com/withparadox2/simpledict/NativeLib"

jint jni_install(JNIEnv* env, jclass clazz, jstring fileName) {
    const char* fileNameChars = env->GetStringUTFChars(fileName, 0);
    int code = install(fileNameChars);
    env->ReleaseStringUTFChars(fileName, fileNameChars);
    return code;
}

jlong jni_prepare(JNIEnv* env, jclass clazz, jstring fileName) {
    const char* fileNameChars = env->GetStringUTFChars(fileName, 0);
    Dict* dict = prepare(fileNameChars);
    env->ReleaseStringUTFChars(fileName, fileNameChars);
    return (jlong)dict;
}

jstring jni_get_content (JNIEnv* env, jclass clazz, jlong wordHandle) {
    Word* word = (Word*)wordHandle;
    string text = word->getContent();
    return env->NewStringUTF(text.c_str());
}

jstring jni_get_dict_name (JNIEnv* env, jclass clazz, jlong wordHandle) {
    Word* word = (Word*)wordHandle;
    string text = word->dict->name;
    return env->NewStringUTF(text.c_str());
}

jobject jni_search(JNIEnv* env, jclass clazz, jstring text) {
    const char* searchText = env->GetStringUTFChars(text, 0);
    auto searchItems = searchSelectedDicts(searchText);
    //TODO cache all these staffs

    jclass list_clazz = env->FindClass("java/util/ArrayList");

    jmethodID list_ctor = env->GetMethodID(list_clazz, "<init>", "()V");
    jobject list_obj = env->NewObject(list_clazz, list_ctor);

    jmethodID list_add = env->GetMethodID(list_clazz, "add", "(Ljava/lang/Object;)Z");

    jclass search_item_clazz = env->FindClass("com/withparadox2/simpledict/SearchItem");
    jmethodID search_item_ctor = env->GetMethodID(search_item_clazz, "<init>", "(Ljava/lang/String;Ljava/util/ArrayList;)V");

    jclass word_clazz = env->FindClass("com/withparadox2/simpledict/Word");
    jmethodID word_ctor = env->GetMethodID(word_clazz, "<init>", "(JLjava/lang/String;)V");

    for (auto& iter : searchItems) {
        jstring str = env->NewStringUTF(iter->text.c_str());
        jobject word_list_obj = env->NewObject(list_clazz, list_ctor);

        for (auto& word : iter->wordList) {
            jobject word_obj = env->NewObject(word_clazz, word_ctor, (jlong)word, str);
            env->CallBooleanMethod(word_list_obj, list_add, word_obj);
        }

        jobject search_item_obj = env->NewObject(search_item_clazz, search_item_ctor, str, word_list_obj);
        env->CallBooleanMethod(list_obj, list_add, search_item_obj);
    }

    env->ReleaseStringUTFChars(text, searchText);
    return list_obj;
}

jboolean jni_active_dict(JNIEnv* env, jclass clazz, jlong dictHandle) {
    Dict* dict = (Dict*) dictHandle;
    if (dict) {
        dict->setSelected(true);
        return JNI_TRUE;
    }
    return JNI_FALSE;
}

jboolean jni_unactive_dict(JNIEnv* env, jclass clazz, jlong dictHandle) {
    Dict* dict = (Dict*) dictHandle;
    if (dict) {
        dict->setSelected(false);
        return JNI_TRUE;
    }
    return JNI_FALSE;
}

static JNINativeMethod gMethods[] = {
    {
        "install",
        "(Ljava/lang/String;)I",
        (void*)jni_install
    },
    {
        "prepare",
        "(Ljava/lang/String;)J",
        (void*)jni_prepare
    },
    {
        "search",
        "(Ljava/lang/String;)Ljava/util/List;",
        (void*)jni_search
    },
    {
        "getContent",
        "(J)Ljava/lang/String;",
        (void*)jni_get_content
    },
    {
        "getDictName",
        "(J)Ljava/lang/String;",
        (void*)jni_get_dict_name
    },
    {
        "activeDict",
        "(J)Z",
        (void*)jni_active_dict
    },
    {
        "unactiveDict",
        "(J)Z",
        (void*)jni_unactive_dict
    }
};

int registerNativeMethods(JNIEnv* env, const char* className, JNINativeMethod* gMethods, int numOfMethods) {
  jclass clazz;
  clazz = env->FindClass(className);
  if (clazz == NULL) {
    return JNI_FALSE;
  }
  if (env->RegisterNatives(clazz, gMethods, numOfMethods) < 0) {
    return JNI_FALSE;
  }
  return JNI_TRUE;
}

int registerNatives(JNIEnv* env) {
  if (!registerNativeMethods(env, JNIREG_CLASS, gMethods,
                             sizeof(gMethods) / sizeof(gMethods[0])))
    return JNI_FALSE;

  return JNI_TRUE;
}

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
  JNIEnv* env = NULL;
  jint result = -1;
  if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
    return -1;
  }
  if (!registerNatives(env)) {
    return -1;
  }
  return JNI_VERSION_1_4;
}
