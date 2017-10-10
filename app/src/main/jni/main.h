#ifndef MAIN_H
#define MAIN_H
#include "dict.h"
#include <memory>
#include "util.h"
#ifdef ANDROID
  #include <jni.h>
#endif


void process(ifstream&, string& inflatedFile, bool prepare);
void readDictionary(ifstream& input, int offset, string& inflatedFile, bool prepare);
void inflate(ifstream& input, vector<int>& deflateStreams, ofstream& ofs);
u8 decompress(ofstream& ofs, ifstream& input, int offset, int length, bool append, vector<char>& bufIn, vector<char>& bufOut);
void testCompress();
void extract(string& inflatedFile, vector<int>& idxArray, int offsetDefs, int offsetXml);
void readWord(ifstream& ifs, Dict* dict, int offsetWords, int offsetXml, int dataLen,
       vector<int>& idxData, int i);
void getIdxData(ifstream& ifs, int position, vector<int>& wordIdxData);
void sortWords(vector<Word*>& wordList);
vector<shared_ptr<SearchItem>> searchSelectedDicts(const char* searchText);
void installDict(const char* path, bool isSelected = true);
string getResFolder(string filePath);
void getSortedDictList(vector<Dict*>& dictList);

//Rely on java to create folder
void createFolder(const char* path); 


/**
 * return: 0 install succed; -1 install failed
 */
int install(const char* filePath, bool prepare = false); 
void release();
Dict* prepare(const char* filePath);

#endif
