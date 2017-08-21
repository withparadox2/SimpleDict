#ifndef MAIN_H
#define MAIN_H
#include "dict.h"


int install(const char* filePath, bool prepare = false); 
void release();
Dict* prepare(const char* filePath);

#endif
