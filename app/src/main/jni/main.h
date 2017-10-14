#ifndef MAIN_H
#define MAIN_H
#include "dict.h"
#include <memory>
#include "util.h"
#ifdef ANDROID
  #include <jni.h>
#endif

void sortWords(vector<Word*>& wordList);

vector<shared_ptr<SearchItem>> searchActiveDicts(const char* searchText);

void installDict(const char* path, bool isActive = true);

string getResFolder(string filePath, string id);

void getSortedDictList(vector<Dict*>& dictList);

//Rely on java to create folder
void createFolder(const char* path); 

/**
 * return: 0 install succed; -1 install failed
 */
int install(const char* filePath, bool prepare = false);

void release();

Dict* prepare(const char* filePath, const char* id);

#endif
