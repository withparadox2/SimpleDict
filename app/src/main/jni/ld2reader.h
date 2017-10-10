#ifndef LD2READER_H_
#define LD2READER_H_
#include <iostream>
#include <fstream>

using namespace std;

class Ld2Extractor {
public:
    const string outputPath;
    ifstream ifs;
    Ld2Extractor(const string inputPath, const string outputPath);

    void process();
    void processRes(int offset);
    void processWords(int offset);
};
#endif
