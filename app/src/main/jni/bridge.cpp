#include "main.h"
#include <jni.h>
#include "log.h"
#include <sstream>


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

    jclass search_item_clazz = env->FindClass("com/withparadox2/simpledict/dict/SearchItem");
    jmethodID search_item_ctor = env->GetMethodID(search_item_clazz, "<init>", "(Ljava/lang/String;Ljava/util/ArrayList;)V");

    jclass word_clazz = env->FindClass("com/withparadox2/simpledict/dict/Word");
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

jboolean jni_activate_dict(JNIEnv* env, jclass clazz, jlong dictHandle) {
    Dict* dict = (Dict*) dictHandle;
    if (dict) {
        dict->setSelected(true);
        return JNI_TRUE;
    }
    return JNI_FALSE;
}

jboolean jni_deactivate_dict(JNIEnv* env, jclass clazz, jlong dictHandle) {
    Dict* dict = (Dict*) dictHandle;
    if (dict) {
        dict->setSelected(false);
        return JNI_TRUE;
    }
    return JNI_FALSE;
}

void jni_set_dict_order(JNIEnv* env, jclass clazz, jlong dictHandle, jint order) {
    Dict* dict = (Dict*) dictHandle;
    if (dict) {
        dict->setOrder(order);
    }
}

void jni_load_res(JNIEnv* env, jclass clazz, jlong wordHandle, jstring resList) {
    Word* word = (Word*)wordHandle;

    const char* resList_str = env->GetStringUTFChars(resList, 0);
    string resListStr(resList_str);

    istringstream ss(resListStr);
    string token;

    vector<string> resVec;
    while(std::getline(ss, token, ';')) {
        resVec.push_back(token);
    }

    word->dict->loadRes(resVec);

    env->ReleaseStringUTFChars(resList, resList_str);
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
        "activateDict",
        "(J)Z",
        (void*)jni_activate_dict
    },
    {
        "deactivateDict",
        "(J)Z",
        (void*)jni_deactivate_dict
    },
    {
        "setDictOrder",
        "(JI)V",
        (void*)jni_set_dict_order
    },
    {
        "loadRes",
        "(JLjava/lang/String;)V",
        (void*)jni_load_res
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

JavaVM* g_JVM;

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    g_JVM = vm;
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


JNIEnv* getJniEnv() {
    JNIEnv* jni_env = 0;
    g_JVM->AttachCurrentThread(&jni_env, 0);
    return jni_env;
}

void createFolder(const char* path) {
    JNIEnv* env = getJniEnv();
    jclass native_clazz = env->FindClass(JNIREG_CLASS);
    jmethodID create_folder_id = env->GetStaticMethodID(native_clazz, "createFolder", "(Ljava/lang/String;)V");
    jstring path_str = env->NewStringUTF(path);
    env->CallStaticVoidMethod(native_clazz, create_folder_id, path_str);
}
