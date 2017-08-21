#include <iostream>
#include <fstream>
#include <string>
#include <memory>
#include <vector>
#include <map>
extern "C" {
#include <zlib.h>
}
#include <string.h>
#include "main.h"

using namespace std;

typedef uint8_t u1;
typedef uint16_t u2;
typedef uint32_t u4;
typedef uint64_t u8;
typedef int8_t s1;
typedef int16_t s2;
typedef int32_t s4;
typedef int64_t s8;

void process(ifstream&, string& inflatedFile, bool prepare);
char* getChars(int len);
void readDictionary(ifstream& input, int offset, string& inflatedFile, bool prepare);
void inflate(ifstream& input, vector<int>& deflateStreams, ofstream& ofs);
u8 decompress(ofstream& ofs, ifstream& input, int offset, int length, bool append);
void testCompress();
void extract(string& inflatedFile, vector<int>& idxArray, int offsetDefs, int offsetXml);
void readWord(ifstream& ifs, Dict* dict, int offsetWords, int offsetXml, int dataLen,
       vector<int>& idxData, int i);
void getIdxData(ifstream& ifs, int position, vector<int>& wordIdxData);

map<string, Dict*> pathToDict;

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
u1 readu1(ifstream& input, int offset) {
    return readNum<u1>(input, offset, 1);
}
u2 readu2(ifstream& input, int offset) {
    return readNum<u2>(input, offset, 2);
}
u4 readu4(ifstream& input, int offset) {
    return readNum<u4>(input, offset, 4);
}
u8 readu8(ifstream& input, int offset) {
    return readNum<u8>(input, offset, 8);
}

u1 readu1(ifstream& input) {
    return readNum<u1>(input, 1);
}
u2 readu2(ifstream& input) {
    return readNum<u2>(input, 2);
}
u4 readu4(ifstream& input) {
    return readNum<u4>(input, 4);
}
u8 readu8(ifstream& input){
    return readNum<u8>(input, 8);
}
int main() {
    string file("newori.ld2");
    install(file.c_str());
    Dict* dict = prepare(file.c_str());
    if (dict) {
        //dict->printWords();
        vector<Word*> list = dict->search("m");
        dict->printWordList(list);
    }
    release();
}

char* getChars(int len) {
    char* chars = new char[len + 1];
    *(chars + len) = 0;
    return chars;
}

void process(ifstream& input, string& inflatedFile, bool prepare = false) {
   input.seekg(0, input.end);
   int length = input.tellg();
   input.seekg(0, input.beg);
   cout << "length : " << length << endl;

   unique_ptr<char> typePtr(getChars(4));
   input.read(typePtr.get(), 4);
   cout << "type : " << typePtr.get() << endl;

   cout << "version : " << readu2(input, 0x18) << "."
        << readu2(input, 0x1a) << endl;

   cout << "id : " << hex << readu8(input, 0x1c) << endl;
   
   int offsetData = readu4(input, 0x5c) + 0x60;
   cout << "info addr : " << offsetData << endl;

   int infoType = readu4(input, offsetData);
   cout << "info type : " << infoType << endl;

   if (infoType == 3) {
       readDictionary(input, offsetData, inflatedFile, prepare);
   }
   //TODO handle other situations
}

void readDictionary(ifstream& input, int offsetWithIndex, string& inflatedFile, 
        bool prepare = false) {
    int limit = readu4(input, offsetWithIndex + 4) + offsetWithIndex + 8;
    int offsetIndex = offsetWithIndex + 0x1C;
    int offsetCompressedDataHeader = readu4(input, offsetWithIndex + 8) + offsetIndex;
    int inflateWordIndexLength = readu4(input, offsetWithIndex + 12);
    int inflateWordsLength = readu4(input, offsetWithIndex + 16);
    int inflateXmlLength = readu4(input, offsetWithIndex + 20);

    int definitions = (offsetCompressedDataHeader - offsetIndex) / 4;

    input.seekg(offsetCompressedDataHeader + 8, input.beg);
    int offset = readu4(input);

    vector<int> deflateStreams;

    if (!prepare) {
        while (offset + input.tellg() < limit) {
            offset = readu4(input);
            deflateStreams.push_back(offset);
        }

        int offsetCompressedData = input.tellg();

        cout << "index words count : " << dec << definitions << endl;
        cout << "index address/size : 0x" << hex << offsetIndex << "/" 
            << dec << (offsetCompressedDataHeader - offsetIndex) << " B" << endl;
        cout << "compressed data address/size : 0x" << hex << offsetCompressedData << "/"
            << dec << limit - offsetCompressedData << " B" << endl;

        ofstream ofs(inflatedFile.c_str(), ofstream::out | ofstream::trunc | ofstream::binary);

        if (ofs.is_open()) {
            inflate(input, deflateStreams, ofs);
            ofs.close();
        }
    } else {
        input.seekg(offsetIndex);
        vector<int> idxArray;
        for(int i = 0; i < definitions; i++) {
            idxArray.push_back(readu4(input));
        }
        extract(inflatedFile, idxArray, inflateWordIndexLength, inflateWordIndexLength + inflateWordsLength);
    }
}


void inflate(ifstream& input, vector<int>& deflateStreams, ofstream& ofs) {
    cout << "inflated file count : " << deflateStreams.size() << endl;
    int startOffset = input.tellg();
    int offset = -1;
    int lastOffset = startOffset;
    bool append = false;
    for (auto iter = deflateStreams.begin(); iter != deflateStreams.end(); iter++) {
        offset = startOffset + *iter;
        decompress(ofs, input, lastOffset, offset - lastOffset, append);
        append = true;
        lastOffset = offset;
    }
}

u8 decompress(ofstream& ofs, ifstream& input, int offset, int length, bool append) {
    char* data = new char[length];
    input.seekg(offset, input.beg);
    input.read(data, length);
    unsigned char* text = reinterpret_cast<unsigned char*>(data);

    //TODO we need to get an exact size of uncompressed data
    unsigned char* buf = new unsigned char[8 * length];
    
    uLong tlen;
    if(uncompress(buf, &tlen, text, length) != Z_OK) {  
        cout << "uncompress failed!\n"; 
    }  

    const char* outData = reinterpret_cast<const char*>(buf);

    ofs.write(outData, tlen);
    delete[] buf;
    delete[] data;
}

void extract(string& inflatedFile, vector<int>& idxArray, int offsetDefs, int offsetXml) {

    Dict* dict = new Dict(inflatedFile);
    ifstream& ifs = dict->ifsInflated;
    if(ifs.is_open()) {
        pathToDict.insert(std::pair<string, Dict*>(inflatedFile, dict));

        int dataLen = 10;
        int defTotal = (offsetDefs / dataLen) - 1;
        
        dict->wordList.reserve(defTotal);

        vector<int> idxData;
        idxData.resize(6);

        ifs.seekg(8, ifs.beg);

        for (int i = 0; i < defTotal; i++) {
            readWord(ifs, dict, offsetDefs, offsetXml, dataLen, idxData, i);
        }
    } else {
        delete dict;
        cout << "not open :" << inflatedFile << endl;
    }
}

void readWord(ifstream& ifs, Dict* dict, int offsetWords, int offsetXml, 
        int dataLen, vector<int>& idxData, int i) {

    getIdxData(ifs, dataLen * i, idxData);
    int lastWordPos = idxData.at(0);
    int lastXmlPos = idxData.at(1);
    int refs = idxData.at(3);
    int currentWordOffset = idxData.at(4);
    int currentXmlOffset = idxData.at(5);

    ifs.seekg(offsetWords + lastWordPos);
    int wordLen = currentWordOffset - lastWordPos;
    unique_ptr<char> wordPtr(getChars(wordLen));
    ifs.read(wordPtr.get(), wordLen);

    Word* word = new Word();
    word->contentPos = offsetXml + lastXmlPos; 
    word->contentSize = currentXmlOffset - lastXmlPos;
    word->text = string(wordPtr.get());
    word->dict = dict;

    dict->wordList.push_back(word);
}

void getIdxData(ifstream& ifs, int position, vector<int>& wordIdxData) {
    ifs.seekg(position, ifs.beg);
    wordIdxData.at(0) = readu4(ifs);
    wordIdxData.at(1) = readu4(ifs);
    wordIdxData.at(2) = readu1(ifs);
    wordIdxData.at(3) = readu1(ifs);
    wordIdxData.at(4) = readu4(ifs);
    wordIdxData.at(5) = readu4(ifs);
}

vector<string> splitFilePath(const char* filePath) {
    string fullPath(filePath);
    vector<string> paths;

    //TODO why "/\\"
    auto splitIndex = fullPath.find_last_of("/\\");
    if (splitIndex != string::npos) {
        paths.push_back(fullPath.substr(0, splitIndex));
        paths.push_back(fullPath.substr(splitIndex + 1));
    } else {
        paths.push_back(fullPath);
    }
    return paths;
}

bool isFileExist(const string& filePath) {
    ifstream ifs(filePath.c_str());
    return ifs.good();
}

int install(const char* filePath, bool force) {
    string ld2Path(filePath);
    string inflatedPath = ld2Path + ".inflated";

    if (isFileExist(inflatedPath) && !force) {
        return 0;
    }

    ifstream is(filePath, ifstream::binary);
    if (is.is_open()) {
        process(is, inflatedPath);
        is.close();
    }

    if (isFileExist(inflatedPath)) {
        return 0;
    } else {
        return -1;
    }
}

void release() {
    for (auto& pair : pathToDict) {
        delete pair.second;
        pathToDict.erase(pair.first);
    }
}

Dict* prepare(const char* filePath) {
    string ld2Path(filePath);
    string inflatedPath = ld2Path + ".inflated";
    if (!isFileExist(inflatedPath)) {
        return nullptr;
    }

    ifstream is(filePath, ifstream::binary);
    if (is.is_open()) {
        process(is, inflatedPath, true);
        is.close();
    }

    auto it = pathToDict.find(inflatedPath);
    if (it != pathToDict.end()) {
        return it->second;
    }
    return nullptr;
}
