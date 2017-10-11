#include "util.h"

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

void write(ofstream& output, int val, int count) {
    char c[count];
    for (int i = count - 1; i >= 0; i--) {
        c[i] = (char)(val >> i * 8);
    }
    output.write(c, count);
}

void write1(ofstream& output, int val) {
    write(output, val, 1);
}
void write2(ofstream& output, int val) {
    write(output, val, 2);
}
void write4(ofstream& output, int val) {
    write(output, val, 4);
}


u4 fromChars(char* ptr, int pos) {
    u4 result = 0;
    ptr += pos;
    for (int i = 0; i < 4; i++) {
        result |= ((u4)(u1)*(ptr + i)) << 8 * i;
    }
    return result;
}

char* getChars(int len) {
    char* chars = new char[len + 1];
    *(chars + len) = 0;
    return chars;
}

