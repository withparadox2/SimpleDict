#ifndef LD2READER_H_
#define LD2READER_H_
#include <iostream>
#include <fstream>
#include <vector>

using namespace std;

class Dict;

class BaseParser {
public:
    const string ld2Path;
    const string sdPath;
    BaseParser(const string ld2Path, const string sdPath);
    void uncompressdata(ifstream& ifs, vector<char>& resultData, int& resultSize, int partCount, int srcPartLen, vector<int>& entryIndexs);
};

class Ld2Extractor : public BaseParser {
private:
    ifstream ifs;
    ofstream ofs;
    void processRes(int offset);
    void processWords(int offset);
    void processCommon(int type, int offset, int keyPos, int keySize, int dataSize, int encryptSize, int decryptSize, int srcPartLen);
public:
    Ld2Extractor(const string ld2Path, const string sdPath);

    int process();
};

class SdReader : public BaseParser {
private:
    ifstream ifsLd2;
    const string resPath;

public:
    SdReader(const string ld2Path, const string sdPath, const string resPath);
    string readDef(int defOffset, int defLen, int partCount, vector<int> entryIndexs, int srcPartLen);
    void readRes(int defOffset, int defLen, int partCount, vector<int> entryIndexs, int srcPartLen, string filePath);
    void readSd();
};
#endif
