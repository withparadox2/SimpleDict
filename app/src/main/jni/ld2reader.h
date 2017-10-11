#ifndef LD2READER_H_
#define LD2READER_H_
#include <iostream>
#include <fstream>
#include <vector>

using namespace std;

class Ld2Extractor {
public:
    const string outputPath;
    ifstream ifs;
    ofstream ofs;
    Ld2Extractor(const string inputPath, const string outputPath);

    void process();
    void processRes(int offset);
    void processWords(int offset);
    void readDef(int defOffset, int defLen, int partCount, vector<int> entryIndexs, int srcPartLen);
    void readRes(int defOffset, int defLen, int partCount, vector<int> entryIndexs, int srcPartLen, string filePath);
    void readSd();
    void uncompressdata(vector<char>& resultData, int& resultSize, int partCount, int srcPartLen, vector<int>& entryIndexs);
};
#endif
