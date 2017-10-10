//Special thanks to Xiaoyun Zhu and http://bbs.pediy.com/thread-209325.htm
#include <iostream>
#include <fstream>
#include <string>
#include <memory>
#include <vector>
#include <map>
#include <algorithm>

extern "C" {
#include <zlib.h>
}
#include <string.h>
#include "main.h"
#include "log.h"

#define RE_CHAR_V(X) reinterpret_cast<char*>(&X[0])
#define RE_UCHAR_V(X) reinterpret_cast<unsigned char*>(&X[0])

using namespace std;

map<string, Dict*> pathToDict;

int main() {
    installDict("Oxford Advanced Learner's English-Chinese Dictionary.ld2");
    //installDict("newori.ld2");
    vector<shared_ptr<SearchItem>> list = searchSelectedDicts("mo");
    release();
}

void installDict(const char* path, bool isSelected) {
    install(path, true);
    Dict* dict = prepare(path);
    if (dict) {
        dict->setSelected(isSelected);
    }
}

void sortWords(vector<Word*>& wordList) {
    SortWord<Word*> sortObj;
    sort(wordList.begin(), wordList.end(), sortObj);
    cout << wordList.size() << endl;
}

void extractRes(ifstream& ifs, string folder, int pos);
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

   cout<< "id : " << hex << readu8(input, 0x1c) << endl;
   
   int sectionPos = 88;

   while (true) {
       if (sectionPos + 8 >= length) {
           break;
       }
       input.seekg(sectionPos, input.beg);
       int type = readu4(input);
       int size = readu4(input);
        
       while (size % 8) {
           size++;
       }

       cout << "type = " << type << endl;
       cout << "sectionPos = " << sectionPos << endl;

       if (type == 3) {
           int offsetData = readu4(input, 0x5c) + 0x60;
           cout << "info addr : " << offsetData << endl;

           int infoType = readu4(input, offsetData);
           cout << "info type : " << infoType << endl;

           if (infoType == 3) {
               readDictionary(input, offsetData, inflatedFile, prepare);
           }
       } else if (type == 4 && !prepare) {
           input.seekg(sectionPos + 8, input.beg);
           extractRes(input, getResFolder(inflatedFile), sectionPos + 8);
       }
       sectionPos += 8 + size;
   }

}

void extractRes(ifstream& ifs, string folder, int pos) {
#ifdef ANDROID
    createFolder(folder.c_str());
#endif
    u4 dwKeyPos = readu4(ifs);
    cout << "dwKeyPos = " << dwKeyPos << endl;
    cout << "cell count = " << (dwKeyPos / 8) << endl;
    u4 dwKeySize = readu4(ifs);
    cout << "dwKeySize = " << dwKeySize << endl;
    cout << "dwDataSize = " << readu4(ifs)<< endl;
    cout << "cbSizeEncrypt = " << readu4(ifs)<< endl;

    u4 dwSizeDecrypt = readu4(ifs);
    cout << "dwSizeDecrypt = " << dwSizeDecrypt << endl;
    u4 dwSrcLen = readu4(ifs);
    cout << "dwSctLen = " << dwSrcLen << endl;

    int partCount = dwSizeDecrypt / dwSrcLen;
    if (dwSizeDecrypt % dwSrcLen != 0) {
        partCount++;
    }
    cout << "partCount = " << partCount << endl;

    pos += 24;
    ifs.seekg(pos, ifs.beg);
    int pre = 0;

    vector<u4> encryIndexs;
    for (int i = 0; i <= partCount; i++) {
        //cout <<hex << "ifs  pos = " << pos + i * 4 << "  tellg = " << endl;
        int current = readu4(ifs);
        //cout <<hex<<current<<"--";
        encryIndexs.push_back(current);

        pre = current;
    }

    //start of encry data
    pos = pos + (partCount + 1) * 4;

    vector<char> resultData;
    int resultSize = 0;
    for (int i = 1; i <= partCount; i++) {
        u4 start = pos + encryIndexs[i-1];
        u4 size = encryIndexs[i] -  encryIndexs[i-1];
        //cout << "encr size = " << size << endl;

        vector<char> buffIn;
        buffIn.resize(size);
        ifs.seekg(start, ifs.beg);
        ifs.read(RE_CHAR_V(buffIn), size);
            


        if (resultData.size() - resultSize < dwSrcLen) {
            resultData.resize(resultSize + dwSrcLen);
        }
        uLong tlen = dwSrcLen;
        unsigned char* buffOutPtr = RE_UCHAR_V(resultData) + resultSize;

        int result = uncompress(buffOutPtr, &tlen, RE_UCHAR_V(buffIn), size);
        //cout << "result = " << result << " result size = "<< tlen << endl;
        resultSize += tlen;
    }
    //cout << "vec sie = " << resultData.size() << " final size = "<< resultSize<< endl; 

    int cellCout = dwKeyPos / 8;

    char *rPtr = RE_CHAR_V(resultData);
    vector<int> keyPosList;
    vector<int> dataPosList;
    for (int i = 0; i < cellCout; i++) {
        int keyPos = fromChars(rPtr, i * 8);
        int dataPos = fromChars(rPtr, i * 8 + 4);
        keyPosList.push_back(keyPos);
        dataPosList.push_back(dataPos);
    }

    for (int i = 1; i < cellCout; i++) {
        int keyPos = keyPosList[i - 1];
        int keyPosEnd = keyPosList[i] ;

        string fileName(rPtr + dwKeyPos + keyPos, keyPosEnd - keyPos);
        //cout << fileName << endl;

        int dataPos = dataPosList[i - 1];
        int dataPosEnd = dataPosList[i];

        string filePath = folder + "/" + fileName;
        ofstream ofs(filePath.c_str(), ofstream::out | ofstream::binary);

        if (ofs.is_open()) {
            ofs.write(rPtr + dwKeyPos + dwKeySize + dataPos, dataPosEnd - dataPos);
            ofs.close();
        }
    }
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

    vector<char> bufOut;
    bufOut.resize(8 * 1024);
    vector<char> bufIn;
    for (auto iter = deflateStreams.begin(); iter != deflateStreams.end(); iter++) {
        offset = startOffset + *iter;
        decompress(ofs, input, lastOffset, offset - lastOffset, append, bufIn, bufOut);
        append = true;
        lastOffset = offset;
    }
}

u8 decompress(ofstream& ofs, ifstream& input, int offset, int length, bool append, 
        vector<char>& bufIn, vector<char>& bufOut) {
    if (bufIn.size() < length) {
        bufIn.resize(length);
    }

    input.seekg(offset, input.beg);
    input.read(RE_CHAR_V(bufIn), length);
    
    uLong tlen = bufOut.size();
    int result;
    while ((result = uncompress(RE_UCHAR_V(bufOut), &tlen, RE_UCHAR_V(bufIn), length)) == Z_BUF_ERROR) {
        bufOut.resize(2 * bufOut.size());
        tlen = bufOut.size();
    }

    if(result != Z_OK) {
        cout << "uncompress failed!" << "result = " << result << endl; 
    } else {
        ofs.write(RE_CHAR_V(bufOut), tlen);
    }  
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
    for (auto iter = pathToDict.begin(); iter != pathToDict.end(); iter++) {
        delete iter->second;
        //TODO crash if invoke erase here
        //pathToDict.erase(iter);
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
        if (it->second) {
            sortWords(it->second->wordList);
        }
        return it->second;
    }
    return nullptr;
}
 
//I make a mistake, with rvo unique_ptr is also acceptable.
vector<shared_ptr<SearchItem>> searchSelectedDicts(const char* searchText) {
    vector<vector<Word*>> allWords;
    vector<Dict*> dictList;
    getSortedDictList(dictList);
    for (Dict* dict : dictList) {
        allWords.push_back(dict->search(searchText));
    }
    
    vector<shared_ptr<SearchItem>> searchList;
    map<string, SearchItem*> wordToPtr;
    for (auto& wordList : allWords) {
        for (auto& wordPtr : wordList) {
            auto cache = wordToPtr.find(wordPtr->text);
            if (cache != wordToPtr.end()) {
                cache->second->wordList.push_back(wordPtr);
            } else {
                shared_ptr<SearchItem> item(new SearchItem);
                item->text = wordPtr->text;
                item->wordList.push_back(wordPtr);
                wordToPtr.insert(std::pair<string, SearchItem*>(item->text, item.get()));
                searchList.push_back(item);
            }
        }
    }

    sort(searchList.begin(), searchList.end(),
            SortWord<shared_ptr<SearchItem>>());
    return searchList;
}

string getResFolder(string filePath) {
    string folder;
    auto split = filePath.find_last_of("/\\");
    string parent;
    string fileName = filePath;
    if (split != string::npos) {
        parent = filePath.substr(0, split);
        fileName = filePath.substr(split + 1);
    }

    split = fileName.find_first_of(".");
    if (split != string::npos) {
        fileName = fileName.substr(0, split);
    } else {
        fileName = fileName + "-res";
    }
    folder = parent + "/" + fileName;
    return folder;
}

void getSortedDictList(vector<Dict*>& dictList) {
    for (auto& pair : pathToDict) {
        Dict* dict = pair.second;
        if (dict && dict->isSelected) {
            dictList.push_back(dict);
        }
    }
    SortDict sortObj;
    sort(dictList.begin(), dictList.end(), sortObj);
}
