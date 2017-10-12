//Special thanks to Xiaoyun Zhu and http://bbs.pediy.com/thread-209325.htm
#include <iostream>
#include <fstream>
#include <string>
#include <memory>
#include <vector>
#include <map>
#include <algorithm>

#include <string.h>
#include "main.h"
#include "log.h"
#include "ld2reader.h"

using namespace std;

map<string, Dict*> pathToDict;

int main() {
    installDict("dict/Oxford Advanced Learner's English-Chinese Dictionary.ld2", true);
}

void installDict(const char* path, bool isActive) {
    int result = install(path, false);
    if (result == 0) {
        Dict* dict = prepare(path);
        if (dict) {
            dict->setIsActive(isActive);
        }
        auto wordList = dict->search("hello");
        dict->printWordList(wordList);
    }
}

void sortWords(vector<Word*>& wordList) {
    SortWord<Word*> sortObj;
    sort(wordList.begin(), wordList.end(), sortObj);
}

bool isFileExist(const string& filePath) {
    ifstream ifs(filePath.c_str());
    return ifs.good();
}

int install(const char* filePath, bool force) {
    string ld2Path(filePath);
    string sdPath = ld2Path + ".sd";

    if (isFileExist(sdPath) && !force) {
        return 0;
    }

    Ld2Extractor ld2(ld2Path, sdPath);
    ld2.process();

    if (isFileExist(sdPath)) {
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
    string sdPath = ld2Path + ".sd";
    if (!isFileExist(sdPath)) {
        return nullptr;
    }

    SdReader* reader = new SdReader(ld2Path, sdPath, getResFolder(sdPath));
    Dict* dict = reader->readSd();
    if (!dict) {
        delete reader;
        return nullptr;
    }

    pathToDict.insert(std::pair<string, Dict*>(sdPath, dict));

    sortWords(dict->wordList);
    return dict;
}
 
//I make a mistake, with rvo unique_ptr is also acceptable.
vector<shared_ptr<SearchItem>> searchActiveDicts(const char* searchText) {
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
        if (dict && dict->isActive) {
            dictList.push_back(dict);
        }
    }
    SortDict sortObj;
    sort(dictList.begin(), dictList.end(), sortObj);
}
