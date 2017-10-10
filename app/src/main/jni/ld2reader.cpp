#include <iostream>
#include <vector>
#include "ld2reader.h"
#include "log.h"
#include "util.h"
extern "C" {
#include <zlib.h>
}
#define RE_CHAR_V(X) reinterpret_cast<char*>(&X[0])
#define RE_UCHAR_V(X) reinterpret_cast<unsigned char*>(&X[0])

Ld2Extractor::Ld2Extractor(const string input, const string output) : outputPath(output) {
    ifs.open(input, ifstream::binary);
    if (ifs.is_open()) {
        process();
        ifs.close();
    }
}

void Ld2Extractor::process() {
    ifs.seekg(0, ifs.end);
    int fileSize = ifs.tellg();

    ifs.seekg(0, ifs.beg);

    int sectionPos = 88;
    while (true) {
       if (sectionPos + 8 >= fileSize) {
           break;
       }
       ifs.seekg(sectionPos, ifs.beg);
       int type = readu4(ifs);
       int size = readu4(ifs);
        
       while (size % 8) {
           size++;
       }

       log("section type = %d\n", type);

       if (type == 3) {
           processWords(sectionPos + 8);
       } else if (type == 4) {
           processRes(sectionPos + 8);
       }
       sectionPos += 8 + size;
    }
}

void Ld2Extractor::processWords(int offset) {
    ifs.seekg(offset, ifs.beg);
    int offsetEncInfo = readu4(ifs);
    int keyPos = readu4(ifs);
    int keySize = readu4(ifs);
    int dataSize = readu4(ifs);
    int encryptSize = readu4(ifs);

    offset += 20 + offsetEncInfo;
    ifs.seekg(offset, ifs.beg);

    int decryptSize = readu4(ifs);
    int srcPartLen = readu4(ifs);
    offset += 8;

    log("otInfo = %d\n", offsetEncInfo);
    log("keyPos = %d\n", keyPos);
    log("keySize = %d\n", keySize);
    log("dataSize = %d\n", dataSize);
    log("encryptSize = %d\n", encryptSize);
    log("srcPartLen = %d\n", srcPartLen);
    log("decryptSize = %d\n", decryptSize);

    int partCount = decryptSize / srcPartLen;
    if (decryptSize % srcPartLen != 0) {
        ++partCount;
    }

    //read EncryptPartInfo
    vector<u4> encryIndexs;
    encryIndexs.reserve(partCount + 1);
    for (int i = 0; i <= partCount; i++) {
        int partInd = readu4(ifs);
        encryIndexs.push_back(partInd);
    }

    offset += 4 * (partCount + 1);

    //uncompress data to origin
    vector<char> orginData;
    int resultSize = 0;
    for (int i = 1; i <= partCount; i++) {
        u4 start = offset + encryIndexs[i-1];
        u4 size = encryIndexs[i] -  encryIndexs[i-1];

        vector<char> buffIn;
        buffIn.resize(size);
        ifs.seekg(start, ifs.beg);
        ifs.read(RE_CHAR_V(buffIn), size);

        if (orginData.size() - resultSize < srcPartLen) {
            orginData.resize(resultSize + srcPartLen);
        }
        uLong tlen = srcPartLen;
        unsigned char* buffOutPtr = RE_UCHAR_V(orginData) + resultSize;

        int result = uncompress(buffOutPtr, &tlen, RE_UCHAR_V(buffIn), size);
        resultSize += tlen;
    }

    //every CellInfo has 10 byte, cellCount = wordCount + 1
    int cellCout = keyPos / 10;

    char *rPtr = RE_CHAR_V(orginData);
    vector<int> keyPosList;
    vector<int> dataPosList;
    for (int i = 0; i < cellCout; i++) {
        int keyPos = fromChars(rPtr, i * 10);
        int dataPos = fromChars(rPtr, i * 10 + 4);
        keyPosList.push_back(keyPos);
        dataPosList.push_back(dataPos);
    }

    ofstream ofs("tttt.txt", ofstream::out);

    for (int i = 1; i < cellCout; i++) {
        int keyPosBeg = keyPosList[i - 1];
        int keyPosEnd = keyPosList[i] ;
        //log("word count = %d\n", keyPosEnd - keyPos);

        string word(rPtr + keyPos + keyPosBeg, keyPosEnd - keyPosBeg);
        ofs.write(rPtr + keyPos + keyPosBeg, keyPosEnd - keyPosBeg);
        //log("word = %s\n", word.c_str());

        int dataPosBeg = dataPosList[i - 1];
        int dataPosEnd = dataPosList[i];

        string def(rPtr + keyPos + keySize + dataPosBeg, dataPosEnd - dataPosBeg);
        ofs.write(rPtr + keyPos + keySize + dataPosBeg, dataPosEnd - dataPosBeg);
        //log("def = %s\n", def.c_str());
    }
    ofs.close();
}

void Ld2Extractor::processRes(int offset) {
}

