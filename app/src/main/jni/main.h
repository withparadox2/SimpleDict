#ifndef MAIN_H
#define MAIN_H
#include "dict.h"

typedef uint8_t u1;
typedef uint16_t u2;
typedef uint32_t u4;
typedef uint64_t u8;
typedef int8_t s1;
typedef int16_t s2;
typedef int32_t s4;
typedef int64_t s8;

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
u1 readu1(ifstream& input, int offset) {
    return readNum<u1>(input, offset, 1);
}
u2 readu2(ifstream& input, int offset) {
    return readNum<u2>(input, offset, 2);
}
u4 readu4(ifstream& input, int offset) {
    return readNum<u4>(input, offset, 4);
}
u8 readu8(ifstream& input, int offset) {
    return readNum<u8>(input, offset, 8);
}

u1 readu1(ifstream& input) {
    return readNum<u1>(input, 1);
}
u2 readu2(ifstream& input) {
    return readNum<u2>(input, 2);
}
u4 readu4(ifstream& input) {
    return readNum<u4>(input, 4);
}
u8 readu8(ifstream& input){
    return readNum<u8>(input, 8);
}

int install(const char* filePath, bool prepare = false); 
void release();
Dict* prepare(const char* filePath);

#endif
