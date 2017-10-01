package com.withparadox2.simpledict.dict;

import java.io.Serializable;

/**
 * Created by withparadox2 on 2017/8/21.
 */

public class Word implements Serializable {
  public long ref;
  public String text;

  public Word(long ref, String text) {
    this.ref = ref;
    this.text = text;
  }
}
