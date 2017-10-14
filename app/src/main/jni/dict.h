#ifndef DICT_H
#define DICT_H

#include <vector>
#include <string>
#include <fstream>
#include <unordered_map>

using namespace std;
class Word;
class SdReader;

class SdCellInfo {
public:
    int defOffset;
    int defLen;
    int defPartCount;
    vector<int> defPartIndexs;
    string filePath;
    SdCellInfo(int defOffset, int defLen, int defPartCount, vector<int>& defPartIndexs);
};

class Dict {
public:
    bool isActive;
    int order;
    vector<Word*> wordList;

    string sdPath;
    string name;
    string id;
    SdReader* reader;
    unordered_map<string, SdCellInfo*> picMap;
    
    Dict(string sdPath, SdReader* reader);
    ~Dict();
    void printWords();

    bool isClose();
    vector<Word*> search(const string& text);
    static void printWordList(vector<Word*>& list);
    void setIsActive(bool isActive);
    void setOrder(int order);
    void loadRes(vector<string> resList);

private:
    int binarySearch(int begin, int end, int index, char c, bool isTop);
    char getWordChar(int wordIndex, int index);

};

class Word {
public:
    Dict* dict;
    string text;
    string getContent();

    //Config for retrieving content from ld2 file
    SdCellInfo* info;
    ~Word();
};

class SearchItem {
public:
    string text;
    vector<Word*> wordList;

    void print();
};

bool sortWord(const string& lh, const string& rh);

// Make sure that lh and rh is a reference to a pointer
// or smart pointer that refers to a member `text`
template <typename T>
struct SortWord {
    bool operator () (T& lh, T& rh) {
        return sortWord(lh->text, rh->text);
    }
};


struct SortDict {
    bool operator () (Dict* lh, Dict* rh) {
       return lh->order < rh->order;
    } 
};
#endif
