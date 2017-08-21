package com.withparadox2.simpledict;

/**
 * Created by withparadox2 on 2017/8/21.
 */

public class Word {
  public long ref;
  public String text;

  public Word(long ref, String text) {
    this.ref = ref;
    this.text = text;
  }

  public Word(String text) {
    this.text = text;
  }

  public Word() {

  }
}
