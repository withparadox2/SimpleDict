#ifndef UTIL_H
#define UTIL_H

#define PRINT_LOG 1

#ifdef ANDROID
    #define  LOG_TAG    "SimpleDict"
    #define  log(...)  if(PRINT_LOG) { __android_log_print(ANDROID_LOG_ERROR, "SimpleDict", __VA_ARGS__); }
	#include <android/log.h>
#else
    #include <stdio.h> 
    #define  log(...) if(PRINT_LOG) { printf(__VA_ARGS__); }
#endif

#endif
