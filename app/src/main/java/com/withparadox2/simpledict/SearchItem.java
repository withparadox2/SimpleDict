package com.withparadox2.simpledict;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by withparadox2 on 2017/8/27.
 */

public class SearchItem implements Serializable {
  String text;
  ArrayList<Word> wordList;
  public SearchItem(String text, ArrayList<Word> wordList) {
    this.text = text;
    this.wordList = wordList;
  }
}
