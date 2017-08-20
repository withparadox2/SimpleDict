#include "dict.h"
#include <iostream>

Dict::Dict(string& inflatedPath) : ifsInflated(inflatedPath.c_str(), ifstream::binary) {}

Dict::~Dict() {
    for (auto word : wordList) {
        delete word;
    }
    ifsInflated.close();
}
Dict::printWords() {
    for(auto iter = wordList.begin(); iter != wordList.end(); iter++) {
        cout << (*iter)->text << endl;
        cout << (*iter)->getContent() << endl;
    }
}
bool Dict::isClose() {
    return ifsInflated.is_open();
}
vector<Word*> Dict::search(const string& text) {
    vector<Word*> result; 
    int i = 0;
    auto iter = wordList.begin();
    while (i < text.size()) {
        bool append = i == text.size() - 1;
        char c = text.at(i);

        while (iter != wordList.end()) {
            string& word = (*iter)->text;
            char cw = word.size() <= i ? 0 : word.at(i);
            if (cw < c) {
                iter++;
            } else if (cw == c) {
                if (append) {
                    result.push_back(*iter);
                    iter++;
                    continue;
                } 
                break;
            } else {
                return result;
            }
        }
        i++;
    }
    return result;
}
void Dict::printWordList(vector<Word*>& wordList) {
    for(auto iter = wordList.begin(); iter != wordList.end(); iter++) {
        cout << (*iter)->text << endl;
        cout << (*iter)->getContent() << endl;
    }
}
string Word::getContent() {
    if (content.size() == 0) {
        readContent();
    }
    return content;
}
void Word::readContent() {
    content.resize(contentSize);
    char* data = const_cast<char*>(content.c_str());
    dict->ifsInflated.seekg(contentPos, dict->ifsInflated.beg);
    dict->ifsInflated.read(data, contentSize);
}
