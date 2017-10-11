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
    ofs.open("dict/t.txt", ofstream::out | ofstream::binary);
    if (ifs.is_open()) {
        process();
        if (ofs.is_open()) {
            ofs.close();
        }
        readSd();
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
    log("partCount = %d\n", partCount);

    //We add offset to encryIndex for simplicity
    offset += 4 * (partCount + 1);

    //read EncryptPartInfo
    vector<int> encryIndexs(partCount + 1);
    for (int i = 0; i <= partCount; i++) {
        int partInd = readu4(ifs) + offset;
        encryIndexs[i] = partInd;
    }

    //uncompress data to origin
    vector<char> originData;
    int resultSize = 0;
    uncompressdata(originData, resultSize, partCount, srcPartLen, encryIndexs);

    //every CellInfo has 10 byte, cellCount = wordCount + 1
    int cellCount = keyPos / 10;

    char *rPtr = RE_CHAR_V(originData);
    vector<int> keyPosList(cellCount);
    vector<int> dataPosList(cellCount);
    for (int i = 0; i < cellCount; i++) {
        int keyPos = fromChars(rPtr, i * 10);
        int dataPos = fromChars(rPtr, i * 10 + 4);
        keyPosList[i] = keyPos;
        dataPosList[i] = dataPos;
    }


    int wordCount = cellCount - 1;
    write1(ofs, 3);

    write4(ofs, wordCount);

    for (int i = 1; i <= wordCount; i++) {
        int keyPosBeg = keyPosList[i - 1];
        int keyPosEnd = keyPosList[i];

        int wordLen = keyPosEnd - keyPosBeg;
        int wordPos = keyPos + keyPosBeg;

        string word(rPtr + wordPos, wordLen);

        //log("word = %s\n", word.c_str());

        int dataPosBeg = dataPosList[i - 1];
        int dataPosEnd = dataPosList[i];

        int defPos = keyPos + keySize + dataPosBeg;
        int defLen = dataPosEnd - dataPosBeg;

        int defPartIndex = defPos / srcPartLen;
        int defOffset = defPos - defPartIndex * srcPartLen;
        int defPartCount = (defOffset + defLen) / srcPartLen;

        if ((defOffset + defLen) % srcPartLen != 0) {
            defPartCount++;
        }

        write1(ofs, wordLen);
        ofs.write(word.c_str(), wordLen);
        write2(ofs, defOffset);
        write4(ofs, defLen);
        write1(ofs, defPartCount);

        //log("part count = %d, partIndex = %d\n", defPartCount, defPartIndex);
        //log("defLen = %d, defPos = %d, defOffset = %d\n", defLen, defPos, defOffset);

        vector<int> partIndexs(defPartCount + 1);

        for (int ii = 0; ii <= defPartCount; ii++) {
            partIndexs[ii] = encryIndexs[ii + defPartIndex];
            write4(ofs, partIndexs[ii]);
        }

        string def(rPtr + defPos, defLen);
    }
}

void Ld2Extractor::processRes(int offset) {
#ifdef ANDROID
    createFolder(folder.c_str());
#endif
    int keyPos = readu4(ifs);
    int keySize = readu4(ifs);
    int dataSize = readu4(ifs);
    int encryptSize = readu4(ifs);

    int decryptSize = readu4(ifs);
    int srcPartLen = readu4(ifs);

    log("keyPos = %d\n", keyPos);
    log("keySize = %d\n", keySize);
    log("dataSize = %d\n", dataSize);
    log("encryptSize = %d\n", encryptSize);
    log("srcPartLen = %d\n", srcPartLen);
    log("decryptSize = %d\n", decryptSize);

    offset += 4 * 6;

    int partCount = decryptSize / srcPartLen;
    if (decryptSize % srcPartLen != 0) {
        ++partCount;
    }

    log("partCount = %d\n", partCount);

    //We add offset to encryIndex for simplicity
    offset += 4 * (partCount + 1);

    vector<int> encryIndexs(partCount + 1);
    for (int i = 0; i <= partCount; i++) {
        int partInd = readu4(ifs) + offset;
        encryIndexs[i] = partInd;
    }

    //uncompress data to origin
    vector<char> originData;
    int resultSize = 0;
    uncompressdata(originData, resultSize, partCount, srcPartLen, encryIndexs);

    log("uncompress reuslt size = %d\n", resultSize);

    //every CellInfo has 10 byte, cellCount = wordCount + 1
    int cellCount = keyPos / 8;

    char *rPtr = RE_CHAR_V(originData);
    vector<int> keyPosList(cellCount);
    vector<int> dataPosList(cellCount);
    for (int i = 0; i < cellCount; i++) {
        int keyPos = fromChars(rPtr, i * 8);
        int dataPos = fromChars(rPtr, i * 8 + 4);
        keyPosList[i] = keyPos;
        dataPosList[i] = dataPos;
    }

    write1(ofs, 4);

    int picCount = cellCount - 1;
    write4(ofs, picCount);

    for (int i = 1; i < cellCount; i++) {
        int keyPosBeg = keyPosList[i - 1];
        int keyPosEnd = keyPosList[i];

        int nameLen = keyPosEnd - keyPosBeg;
        int namePos = keyPos + keyPosBeg;

        string fileName(rPtr + namePos, nameLen);
        
        //log("i = %d, fineName = %s\n", i, fileName.c_str());

        int dataPosBeg = dataPosList[i - 1];
        int dataPosEnd = dataPosList[i];

        int defPos = keyPos + keySize + dataPosBeg;
        int defLen = dataPosEnd - dataPosBeg;

        int defPartIndex = defPos / srcPartLen;
        int defOffset = defPos - defPartIndex * srcPartLen;
        int defPartCount = (defOffset + defLen) / srcPartLen;

        if ((defOffset + defLen) % srcPartLen != 0) {
            defPartCount++;
        }

        write1(ofs, nameLen);
        ofs.write(fileName.c_str(), nameLen);
        write2(ofs, defOffset);
        write4(ofs, defLen);
        write1(ofs, defPartCount);

        vector<int> partIndexs(defPartCount + 1);

        for (int ii = 0; ii <= defPartCount; ii++) {
            partIndexs[ii] = encryIndexs[ii + defPartIndex];
            write4(ofs, partIndexs[ii]);
        }
    }
}

void Ld2Extractor::readDef(int defOffset, int defLen, int partCount, vector<int> encryIndexs, int srcPartLen) {
    vector<char> originData;
    int resultSize = 0;
    uncompressdata(originData, resultSize, partCount, srcPartLen, encryIndexs);
    char *rPtr = RE_CHAR_V(originData);
    string def(rPtr + defOffset, defLen);
    //log("def = %s\n\n\n", def.c_str());
}

void Ld2Extractor::readRes(int defOffset, int defLen, int partCount, vector<int> encryIndexs, int srcPartLen, string filePath) {
    vector<char> originData;
    int resultSize = 0;
    uncompressdata(originData, resultSize, partCount, srcPartLen, encryIndexs);
    log("res size = %d\n",  resultSize);
    char *rPtr = RE_CHAR_V(originData);
    ofstream ofsn(filePath, ofstream::binary | ofstream::out);
    ofsn.write(rPtr + defOffset, defLen);
    ofsn.close();
}

void Ld2Extractor::readSd() {
    ifstream ifsn;
    ifsn.open("dict/t.txt", ifstream::binary);
    if (!ifsn.is_open()) {
        return;
    }

    ifsn.seekg(0, ifsn.beg);
    int type = readu1(ifsn);
    log("read type = %d\n", type);
    int wordCount = readu4(ifsn);
    log("word count = %d\n", wordCount);

    for (int i = 0; i < wordCount; i++) {
        int wordLen = readu1(ifsn);
        string word(wordLen, ' ');
        ifsn.read(const_cast<char*>(word.c_str()), wordLen);
        //log("word = %s\n", word.c_str());

        int defOffset = readu2(ifsn);
        int defLen = readu4(ifsn);
        int defPartCount = readu1(ifsn);

        vector<int> partIndexs(defPartCount + 1);

        for (int ii = 0; ii <= defPartCount; ii++) {
            partIndexs[ii] = readu4(ifsn);
        }
        //readDef(defOffset, defLen, defPartCount, partIndexs, 0x4000);
    }
    type = readu1(ifsn);
    log("read type = %d\n", type);
    int picCount = readu4(ifsn);
    log("pic count = %d\n", picCount);

    for (int i = 0; i < picCount; i++) {
        int wordLen = readu1(ifsn);
        string word(wordLen, ' ');
        ifsn.read(const_cast<char*>(word.c_str()), wordLen);

        int defOffset = readu2(ifsn);
        int defLen = readu4(ifsn);
        int defPartCount = readu1(ifsn);

        vector<int> partIndexs(defPartCount + 1);

        for (int ii = 0; ii <= defPartCount; ii++) {
            partIndexs[ii] = readu4(ifsn);
        }
        string path = "dict/pic/" + word;
        log("i = %d, pic name = %s, partCount = %d\n", i, word.c_str(), defPartCount);

        readRes(defOffset, defLen, defPartCount, partIndexs, 0x4000, path);
    }

    ifsn.close();
}

void Ld2Extractor::uncompressdata(vector<char>& resultData, int& resultSize, int partCount, int srcPartLen, vector<int>& encryIndexs) {
    resultData.resize(partCount * srcPartLen);
    resultSize = 0;
    vector<char> buffIn;
    for (int i = 1; i <= partCount; i++) {
        u4 start = encryIndexs[i-1];
        u4 size = encryIndexs[i] -  encryIndexs[i-1];
        if (buffIn.size() < size) {
            buffIn.resize(size);
        }

        ifs.seekg(start, ifs.beg);
        ifs.read(RE_CHAR_V(buffIn), size);

        uLong tlen = srcPartLen;
        unsigned char* buffOutPtr = RE_UCHAR_V(resultData) + resultSize;

        int result = uncompress(buffOutPtr, &tlen, RE_UCHAR_V(buffIn), size);
        resultSize += tlen;
    }
}
