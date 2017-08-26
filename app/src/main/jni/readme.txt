1, Install: 
     1.1, Native decompresses ld2 file passed from Java into a inflated file with same name as ld2 file

2, Prepare: 
     2.1, Java passes to native ld2 file path by which native constructs dict struct and reads inflated file
     2.2, Native collects all words from inflated file and stores them into a dict struct
     2.3, Native returns dict struct address to java, java should keep all these dict infos
     2.4, The dicts selected by user should also be passed to native, which
will contribute in stage #3

3, Search:
     3.1, Java passes search-text and dict structs to native, if the dict isnot specified in one search-action, then native will use the selected dicts passed in stage #2.4
     3.2, Native searches all dicts and builds a list of word consisting the required search-text 
     3.3, The item contained in list reaturned to java should keep index to each native-word found in different dicts 

4, Content:
     4.1, Java passes indexs to words to native
     4.2, Native reads content from inflated file by using content-offset stored in word and caches it into the same word

5, Release

6, Uninstall

