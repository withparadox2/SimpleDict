#ifndef DICT_H
#define DICT_H

#include <vector>
#include <string>
#include <fstream>

using namespace std;
class Word;
class Dict {
public:
    bool isSelected;
    int order;
    vector<Word*> wordList;

    ifstream ifsInflated;
    string inflatedPath;
    string name;
    Dict(string& inflatedPath);
    ~Dict();
    void printWords();

    bool isClose();
    vector<Word*> search(const string& text);
    static void printWordList(vector<Word*>& list);
    void setSelected(bool isSelected);
    void setOrder(int order);

private:
    int binarySearch(int begin, int end, int index, char c, bool isTop);
    char getWordChar(int wordIndex, int index);
};

class Word {
public:
    int contentPos;
    int contentSize;
    string text;
    string content;
    Dict* dict;
    string getContent();
    void readContent();
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
