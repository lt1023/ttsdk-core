#include <jni.h>
#include <string>
//#include <android/log.h>


#define TARGET_CLASS "com/linktower/application/GameSDK"
#define TARGET_METHOD_ONCREATE "onCreate"
#define TARGET_ONCREATE_SIG "(Landroid/app/Application;)V"
#define TARGET_METHOD_INIT "init"
#define TARGET_INIT_SIG "(Landroid/content/Context;)V"
#define TARGET_METHOD_GETNAME "getName"
#define TARGET_GETNAME_SIG "()Ljava/lang/String;"
//
///**
// * 注册JNI
// */
//static const JNINativeMethod gMethods[] = { { TARGET_CRYPT, TARGET_CRYPT_SIG,
//                                                    (void*) android_native_aes }, { TARGET_READ, TARGET_READ_SIG,
//                                                    (void*) android_native_read } };
//
//JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
//    JNIEnv* env = NULL;
//    if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK) {
//        return -1;
//    }
//
//    jclass clazz = (*env)->FindClass(env, TARGET_CLASS);
//    if (!clazz) {
//        return -1;
//    }
//    //这里就是关键了，把本地函数和一个java类方法关联起来。不管之前是否关联过，一律把之前的替换掉！
//    if ((*env)->RegisterNatives(env, clazz, gMethods,
//                                sizeof(gMethods) / sizeof(gMethods[0])) != JNI_OK) {
//        return -1;
//    }
//
//    return JNI_VERSION_1_4;
//}
//
//JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved) {
//    JNIEnv* env = NULL;
//    if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK) {
//        return;
//    }
//}
//extern "C"
//JNIEXPORT void JNICALL
//hello_one(JNIEnv *env, jclass clazz, jobject application) {
//    jclass TTAdManagerHolder = env->FindClass("com/linktower/games/TTAdManagerHolder");
//    jmethodID init = env->GetStaticMethodID(TTAdManagerHolder, "init",
//                                            "(Landroid/content/Context;)V");
//    env->CallStaticVoidMethod(TTAdManagerHolder, init, application);
//}

//extern "C"
//JNIEXPORT void JNICALL
//hello_two(JNIEnv *env, jclass clazz, jobject base) {
//
//}
//extern "C"
//JNIEXPORT void JNICALL
//hello_two(JNIEnv *env, jclass clazz, jobject base) {
//
//}

//extern "C"
//JNIEXPORT jstring JNICALL
//hello_three(JNIEnv *env, jclass clazz) {
////    std::string hello = "tt_83406806ff99500e8e9cffc9875c2a";
//    std::string hello = "dHRfODM0MDY4MDZmZjk5NTAwZThlOWNmZmM5ODc1YzJh";
//    return env->NewStringUTF(hello.c_str());
//}

void hello_one(JNIEnv *env, jclass clazz, jobject application) {
    jclass TTAdManagerHolder = env->FindClass("com/linktower/games/TTAdManagerHolder");
    jmethodID init = env->GetStaticMethodID(TTAdManagerHolder, "init",
                                            "(Landroid/content/Context;)V");
    env->CallStaticVoidMethod(TTAdManagerHolder, init, application);
}

void hello_two(JNIEnv *env, jclass clazz, jobject base) {

}


jstring hello_three(JNIEnv *env, jclass clazz) {
//    std::string hello = "tt_83406806ff99500e8e9cffc9875c2a";
    std::string hello = "dHRfODM0MDY4MDZmZjk5NTAwZThlOWNmZmM5ODc1YzJh";
    return env->NewStringUTF(hello.c_str());
}


static const JNINativeMethod jniNativeMethod[] = {
        {TARGET_METHOD_ONCREATE, TARGET_ONCREATE_SIG, (void *) hello_one},
        {TARGET_METHOD_INIT,     TARGET_INIT_SIG,     (void *) hello_two},
        {TARGET_METHOD_GETNAME,  TARGET_GETNAME_SIG,  (void *) hello_three},
};


JavaVM *jvm = nullptr;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *javaVm, void *pVoid) {
    jvm = javaVm;
    JNIEnv *jniEnv = nullptr;
    jint result = javaVm->GetEnv(reinterpret_cast<void **>(&jniEnv), JNI_VERSION_1_6);
    if (result != JNI_OK) {
        return -1;
    }
    jclass jniclass = jniEnv->FindClass(TARGET_CLASS);
    if (jniEnv->RegisterNatives(jniclass, jniNativeMethod,
                                sizeof(jniNativeMethod) / sizeof(jniNativeMethod[0])) < 0) {
        return JNI_FALSE;
    }
    return JNI_VERSION_1_6;
}