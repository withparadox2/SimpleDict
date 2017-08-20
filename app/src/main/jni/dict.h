/*
 * 1, Install: 
 *      1.1, Native decompresses ld2 file passed from Java into a inflated file with same name as ld2 file
 * 2, Parse: 
 *      2.1, Java passes to native ld2 file path by which native constructs dict struct and reads inflated file
 *      2.2, Native collects all words from inflated file and stores them into a dict struct
 *      2.3, Native returns dict struct address to java, java should keep all these dict infos
 * 3, Search:
 *      3.1, Java passes search-text and dict structs to native
 *      3.2, Native searches all dicts and builds a list of word consisting the required search-text 
 *      3.3, The item of list reaturned to java should keep index to each word found in different dicts 
 * 4, Content:
 *      4.1, Java passes indexs to words to native
 *      4.2, Native reads content from inflated file and cache it into a word struct stored in an array built at stage 2.2
 * 5, Release
 * 6, Uninstall
 */

#ifndef DICT_H
#define DICT_H

#include <vector>
#include <string>
#include <fstream>

using namespace std;
class Word;
class Dict {
public:
    Dict(string& inflatedPath);
    vector<Word*> wordList;

    ifstream ifsInflated;
    ~Dict();
    printWords();

    bool isClose();
    vector<Word*> search(const string& text);
    void printWordList(vector<Word*>& list);
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

#endif
