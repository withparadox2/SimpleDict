#include "dict.h"
#include "ld2reader.h"
#include <iostream>
#include "log.h"

Dict::Dict(string sdPath, SdReader* reader) : reader(reader), sdPath(sdPath), isSelected(false) {
    auto from = sdPath.find_last_of("/\\");
    auto to = sdPath.find(".ld2");
    from = from == string::npos ? 0 : from + 1;
    if (to != string::npos) {
        this->name = sdPath.substr(from, to - from);
    } else {
        this->name = sdPath;
    }
}

Dict::~Dict() {
    for (auto word : wordList) {
        delete word;
    }
    for (auto iter = picMap.begin(); iter != picMap.end(); iter++) {
        delete iter->second;
    }
    delete reader;
}

void Dict::printWords() {
    int times = 0;
    for(auto iter = wordList.begin(); iter != wordList.end(); iter++) {
        cout << (*iter)->text << endl;
        //cout << (*iter)->getContent() << endl;
    }
}
bool Dict::isClose() {
    return false;
}

int Dict::binarySearch(int begin, int end, int index, char c, bool isTop) {
    
    if (begin == end) {
        if (getWordChar(begin, index) != c) {
            return -1;
        } else {
            return begin;
        }
    } else if (end - begin == 1) {
        char bc = getWordChar(begin, index);
        char ec = getWordChar(end, index);
        if (isTop) {
            if (bc == c) {
                return begin;
            } else if (ec == c) {
                return end;
            } else {
                return -1;
            }
        } else {
            if (ec == c) {
                return end;
            } else if (bc == c) {
                return begin;
            } else {
                return -1;
            }
        }
    }

    int mid = begin + (end - begin) / 2;
    char midChar = getWordChar(mid, index);

    if (midChar < c) {
        return binarySearch(mid, end, index, c, isTop);
    } else if(midChar > c) {
        return binarySearch(begin, mid, index, c, isTop);
    } else {
        if (isTop) {
            return binarySearch(begin, mid, index, c, isTop);
        } else {
            return binarySearch(mid, end, index, c, isTop);
        }
    }
}

char Dict::getWordChar(int wordIndex, int index) {
    string& word = wordList.at(wordIndex)->text;
    return word.size() <= index ? 0 : word.at(index);
}

vector<Word*> Dict::search(const string& text) {
   vector<Word*> result; 
   int i = 0;
   int ib = 0;
   int ie = wordList.size() - 1;
   for (i = 0; i < text.size(); i++) {
       char c = text.at(i);
       ib = binarySearch(ib, ie, i, c, true);
       if (ib == -1) {
           return result;
       }
       ie = binarySearch(ib, ie, i, c, false);
       if (ie == -1) {
           return result;
       }

       if (i == text.size() - 1) {
           for (int s = ib; s <= ie && s <= 10 + ib; s++) {
             result.push_back(wordList.at(s));
           }
       }
   }
   return result;

}

void Dict::printWordList(vector<Word*>& wordList) {
    for(auto iter = wordList.begin(); iter != wordList.end(); iter++) {
        cout << (*iter)->text << endl;
        cout << (*iter)->getContent() << endl;
    }
}

void Dict::setSelected(bool isSelected) {
    this->isSelected = isSelected;
}

void Dict::setOrder(int order) {
    this->order = order;
}

void Dict::loadRes(vector<string> resList) {
    for (string name : resList) {
        auto it = picMap.find(name);
        if (it != picMap.end()) {
            ResInfo* info = it->second;
            reader->readRes(info->defOffset, info->defLen, info->defPartCount, 
                    info->defPartIndexs, 0x4000, info->filePath);
        }
    }
}

string Word::getContent() {
    if (content.size() == 0) {
        readContent();
    }
    return content;
}
void Word::readContent() {
//   content.resize(contentSize);
//   char* data = const_cast<char*>(content.c_str());
//   dict->ifsInflated.seekg(contentPos, dict->ifsInflated.beg);
//   dict->ifsInflated.read(data, contentSize);
    
    content = dict->reader->readDef(defOffset, defLen, defPartCount, defPartIndexs, 0x4000);
}

void SearchItem::print() {
    cout << "=============" << endl;
    cout << this->text << endl;
    for (auto word : wordList) {
        cout << word->dict->sdPath << endl;
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
