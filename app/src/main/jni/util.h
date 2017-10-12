#ifndef UTIL_H_
#define UTIL_H_
#include <fstream>
using namespace std;

typedef uint8_t u1;
typedef uint16_t u2;
typedef uint32_t u4;
typedef uint64_t u8;
typedef int8_t s1;
typedef int16_t s2;
typedef int32_t s4;
typedef int64_t s8;

u1 readu1(ifstream& input, int offset);
u2 readu2(ifstream& input, int offset);
u4 readu4(ifstream& input, int offset);
u8 readu8(ifstream& input, int offset);

u1 readu1(ifstream& input);
u2 readu2(ifstream& input);
u4 readu4(ifstream& input);
u8 readu8(ifstream& input);

u4 fromChars(char* ptr, int pos);

void write(ofstream& output, int val, int count);
void write1(ofstream& output, int val);
void write2(ofstream& output, int val);
void write4(ofstream& output, int val);

char* getChars(int len);

template<typename T> 
T readNum(ifstream& input, int len) {
    char* cs = getChars(len);    
    input.read(cs, len);
    T result = 0;
    for (int i = 0; i < len; i++) {
        result |= ((T)(u1)*(cs + i)) << 8 * i;
    }
    delete[] cs;
    return result;
}


template<typename T>
T readNum(ifstream& input, int offset, int len) {
    input.seekg(offset, input.beg);
    readNum<T>(input, len);
}

int getStreamSize(ifstream& ifs);

#endif
