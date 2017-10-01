#ifndef MAIN_H
#define MAIN_H
#include "dict.h"
#include <memory>
#include <jni.h>

typedef uint8_t u1;
typedef uint16_t u2;
typedef uint32_t u4;
typedef uint64_t u8;
typedef int8_t s1;
typedef int16_t s2;
typedef int32_t s4;
typedef int64_t s8;


void process(ifstream&, string& inflatedFile, bool prepare);
void readDictionary(ifstream& input, int offset, string& inflatedFile, bool prepare);
void inflate(ifstream& input, vector<int>& deflateStreams, ofstream& ofs);
u8 decompress(ofstream& ofs, ifstream& input, int offset, int length, bool append, vector<char>& bufIn, vector<char>& bufOut);
void testCompress();
void extract(string& inflatedFile, vector<int>& idxArray, int offsetDefs, int offsetXml);
void readWord(ifstream& ifs, Dict* dict, int offsetWords, int offsetXml, int dataLen,
       vector<int>& idxData, int i);
void getIdxData(ifstream& ifs, int position, vector<int>& wordIdxData);
char* getChars(int len);
void sortWords(vector<Word*>& wordList);
vector<shared_ptr<SearchItem>> searchSelectedDicts(const char* searchText);
void installDict(const char* path, bool isSelected = true);
string getResFolder(string filePath);

//Rely on java to create folder
void createFolder(const char* path); 

u1 readu1(ifstream& input, int offset);
u2 readu2(ifstream& input, int offset);
u4 readu4(ifstream& input, int offset);
u8 readu8(ifstream& input, int offset);

u1 readu1(ifstream& input);
u2 readu2(ifstream& input);
u4 readu4(ifstream& input);
u8 readu8(ifstream& input);

u4 fromChars(char* ptr, int pos);

template<typename T> 
T readNum(ifstream& input, int len) {
    char* cs = getChars(len);    
    input.read(cs, len);
    T result = 0;
    for (int i = 0; i < len; i++) {
        result |= ((T)(u1)*(cs + i)) << 8 * i;
    }
    delete[] cs;
    return result;
}


template<typename T>
T readNum(ifstream& input, int offset, int len) {
    input.seekg(offset, input.beg);
    readNum<T>(input, len);
}

/**
 * return: 0 install succed; -1 install failed
 */
int install(const char* filePath, bool prepare = false); 
void release();
Dict* prepare(const char* filePath);

#endif
