#include <iostream>
#include <vector>
#include "ld2reader.h"
#include "log.h"
#include "util.h"
#include "dict.h"
extern "C" {
#include <zlib.h>
}
#define RE_CHAR_V(X) reinterpret_cast<char*>(&X[0])
#define RE_UCHAR_V(X) reinterpret_cast<unsigned char*>(&X[0])

BaseParser::BaseParser(const string ld2Path, const string sdPath) 
    : sdPath(sdPath), ld2Path(ld2Path) {}

void BaseParser::uncompressdata(ifstream& ifs, vector<char>& resultData, int& resultSize, int partCount, int srcPartLen, vector<int>& encryIndexs) {
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

Ld2Extractor::Ld2Extractor(const string ld2Path, const string sdPath) 
    : BaseParser(ld2Path, sdPath) {
}

int Ld2Extractor::process() {
    ifs.open(ld2Path.c_str(), ifstream::binary);
    if (!ifs.is_open()) {
        log("can not open file: %s\n", ld2Path.c_str());
        return -1;
    }

    ofs.open(sdPath.c_str(), ofstream::out | ofstream::binary);
    if (!ofs.is_open()) {
        log("can not open file: %s\n", sdPath.c_str());
        return -1;
    }

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
        
       //align to 8 byte
       while (size % 8) {
           size++;
       }

       if (type == 3) {
           processWords(sectionPos + 8);
       } else if (type == 4) {
           processRes(sectionPos + 8);
       }
       sectionPos += 8 + size;
    }

    ifs.close();
    ofs.close();
    return 0;
}

void Ld2Extractor::processWords(int offset) {
    ifs.seekg(offset, ifs.beg);
    //header 5 * 4 = 20
    int offsetEncInfo = readu4(ifs);
    int keyPos = readu4(ifs);
    int keySize = readu4(ifs);
    int dataSize = readu4(ifs);
    int encryptSize = readu4(ifs);

    //skip header and word index(wordCount * 4)
    offset += 20 + offsetEncInfo;
    ifs.seekg(offset, ifs.beg);

    //encrypt header 2 * 4 = 8
    int decryptSize = readu4(ifs);
    int srcPartLen = readu4(ifs);
    offset += 8;

    processCommon(3, offset, keyPos, keySize, dataSize, encryptSize, decryptSize, srcPartLen);
}

void Ld2Extractor::processRes(int offset) {
    //header 4 * 4 
    int keyPos = readu4(ifs);
    int keySize = readu4(ifs);
    int dataSize = readu4(ifs);
    int encryptSize = readu4(ifs);

    //encrypt header 2 * 4
    int decryptSize = readu4(ifs);
    int srcPartLen = readu4(ifs);

    //skip two headers
    offset += 4 * 6;
    processCommon(4, offset, keyPos, keySize, dataSize, encryptSize, decryptSize, srcPartLen);
}

void Ld2Extractor::processCommon(int type, int offset, int keyPos, int keySize, int dataSize, int encryptSize, int decryptSize, int srcPartLen) {
    int partCount = decryptSize / srcPartLen;
    if (decryptSize % srcPartLen != 0) {
        ++partCount;
    }

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
    uncompressdata(ifs, originData, resultSize, partCount, srcPartLen, encryIndexs);

    //every WordCellInfo has 10 byte, ResCellInfo has 8 byte
    //cellCount = wordCount + 1
    int cellInfoSize = type == 3 ? 10 : 8;
    int cellCount = keyPos / cellInfoSize;

    char *rPtr = RE_CHAR_V(originData);
    vector<int> keyPosList(cellCount);
    vector<int> dataPosList(cellCount);
    for (int i = 0; i < cellCount; i++) {
        int keyPos = fromChars(rPtr, i * cellInfoSize);
        int dataPos = fromChars(rPtr, i * cellInfoSize + 4);
        keyPosList[i] = keyPos;
        dataPosList[i] = dataPos;
    }

    write1(ofs, type);

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

SdReader::SdReader(const string ld2Path, const string sdPath, const string resPath) 
    : BaseParser(ld2Path, sdPath), resPath(resPath) {
}

string SdReader::readDef(int defOffset, int defLen, int partCount, vector<int> encryIndexs, int srcPartLen) {
    vector<char> originData;
    int resultSize = 0;
    uncompressdata(ifsLd2, originData, resultSize, partCount, srcPartLen, encryIndexs);
    char *rPtr = RE_CHAR_V(originData);
    string def(rPtr + defOffset, defLen);
    return def;
}

void SdReader::readRes(int defOffset, int defLen, int partCount, vector<int> encryIndexs, int srcPartLen, string filePath) {
    vector<char> originData;
    int resultSize = 0;
    uncompressdata(ifsLd2, originData, resultSize, partCount, srcPartLen, encryIndexs);
    char *rPtr = RE_CHAR_V(originData);
    ofstream ofsn(filePath, ofstream::binary | ofstream::out);
    ofsn.write(rPtr + defOffset, defLen);
    ofsn.close();
}

Dict* SdReader::readSd() {
    ifstream ifsSd(sdPath, ifstream::binary);
    if (!ifsSd.is_open()) {
        return nullptr;
    }
    ifsLd2.open(ld2Path, ifstream::binary);
    if (!ifsLd2.is_open()) {
        return nullptr;
    }

    ifsSd.seekg(0, ifsSd.end);
    int fileSize = ifsSd.tellg();

    ifsSd.seekg(0, ifsSd.beg);

    Dict* dict = new Dict(sdPath, this);
    while(true) {
        if (ifsSd.tellg() >= fileSize) {
            break;
        }

        int type = readu1(ifsSd);
        log("read type = %d\n", type);
        int itemCount = readu4(ifsSd);
        log("item count = %d\n", itemCount);

        for (int i = 0; i < itemCount; i++) {
            int wordLen = readu1(ifsSd);
            string text(wordLen, ' ');
            ifsSd.read(const_cast<char*>(text.c_str()), wordLen);

            int defOffset = readu2(ifsSd);
            int defLen = readu4(ifsSd);
            int defPartCount = readu1(ifsSd);

            vector<int> partIndexs(defPartCount + 1);

            for (int ii = 0; ii <= defPartCount; ii++) {
                partIndexs[ii] = readu4(ifsSd);
            }
            if (type == 4) {
                string path = resPath + "/" + text;
                //log("content = %s\n", path.c_str());
                //readRes(defOffset, defLen, defPartCount, partIndexs, 0x4000, path);
                ResInfo* info = new ResInfo;
                info->defOffset = defOffset;
                info->defLen = defLen;
                info->defPartCount = defPartCount;
                info->defPartIndexs = partIndexs;
                info->filePath = path;
                dict->picMap.insert(std::pair<string, ResInfo*>(text, info));
            } else if (type == 3) {
                Word* word = new Word();
                word->text = text;
                word->dict = dict;
                word->defOffset = defOffset;
                word->defLen = defLen;
                word->defPartCount = defPartCount;
                word->defPartIndexs = partIndexs;
                dict->wordList.push_back(word);

                //string defcontent = readDef(defOffset, defLen, defPartCount, partIndexs, 0x4000);
                //log("content = %s\n", defcontent.c_str());
            }
        }
    }
    ifsSd.close();
    return dict;
}
