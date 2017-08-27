#include "dict.h"
#include <iostream>

Dict::Dict(string& inflatedPath) : ifsInflated(inflatedPath.c_str(), ifstream::binary), inflatedPath(inflatedPath), isSelected(false) {
    auto from = inflatedPath.find_last_of("/\\");
    auto to = inflatedPath.find(".ld2");
    from = from == string::npos ? 0 : from + 1;
    if (to != string::npos) {
        this->name = inflatedPath.substr(from, to - from);
    } else {
        this->name = inflatedPath;
    }
}

Dict::~Dict() {
    for (auto word : wordList) {
        delete word;
    }
    ifsInflated.close();
}
void Dict::printWords() {
    int times = 0;
    for(auto iter = wordList.begin(); iter != wordList.end(); iter++) {
        cout << (*iter)->text << endl;
        //cout << (*iter)->getContent() << endl;
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
                    if (result.size() > 10) {
                        return result;
                    }
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
        //cout << (*iter)->getContent() << endl;
    }
}

void Dict::setSelected(bool isSelected) {
    this->isSelected = isSelected;
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

void SearchItem::print() {
    cout << "=============" << endl;
    cout << this->text << endl;
    for (auto word : wordList) {
        cout << word->dict->inflatedPath << endl;
    }
}

bool sortWord(const string& lh, const string& rh) {
    int lhSize = lh.size();
    int rhSize = rh.size();
    int i = 0;
    while (i < lhSize && i < rhSize) {
        if (lh[i] == rh[i]) {
            i++;
        } else {
            return lh[i] < rh[i];
        }
    }
    //TODO return true will crash, why?
    return lhSize < rhSize;
}
