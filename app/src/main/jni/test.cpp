#include "ld2reader.h"
int main() {
    //Ld2Extractor ld2("dict/newori.ld2", "123");
    Ld2Extractor ld2("dict/Oxford+Advanced+Learner's+English-Chinese+Dictionary.ld2", "dict/oale.sd");
    //ld2.process();
    SdReader reader("dict/Oxford+Advanced+Learner's+English-Chinese+Dictionary.ld2", "dict/oale.sd", "dict/pic");
    reader.readSd();
}
