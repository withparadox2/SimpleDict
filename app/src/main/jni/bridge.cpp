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

jobject jni_search(JNIEnv* env, jclass clazz, jlong dictHandle, jstring text) {
    const char* searchText = env->GetStringUTFChars(text, 0);
    Dict* dict = (Dict*)dictHandle;
    vector<Word*> words = dict->search(searchText);

    jclass list_clazz = env->FindClass("java/util/ArrayList");

    jmethodID list_ctor = env->GetMethodID(list_clazz, "<init>", "()V");
    jobject list_obj = env->NewObject(list_clazz, list_ctor);

    jmethodID list_add = env->GetMethodID(list_clazz, "add", "(Ljava/lang/Object;)Z");

    jclass word_clazz = env->FindClass("com/withparadox2/simpledict/Word");
    jmethodID word_ctor = env->GetMethodID(word_clazz, "<init>", "(JLjava/lang/String;)V");

    for (auto iter = words.begin(); iter != words.end(); iter++) {
        jstring str = env->NewStringUTF((*iter)->text.c_str());
        jobject word_obj = env->NewObject(word_clazz, word_ctor, (jlong)*iter, str);
        env->CallBooleanMethod(list_obj, list_add, word_obj);
    }

    env->ReleaseStringUTFChars(text, searchText);
    return list_obj;

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
        "(JLjava/lang/String;)Ljava/util/List;",
        (void*)jni_search
    },
    {
        "getContent",
        "(J)Ljava/lang/String;",
        (void*)jni_get_content
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
