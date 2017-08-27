package com.withparadox2.simpledict;

import java.util.List;

/**
 * Created by withparadox2 on 2017/8/21.
 */

public class NativeLib {
  static {
    System.loadLibrary("dict");
  }

  public static native int install(String file);
  public static native long prepare(String file);
  public static native List<SearchItem> search(String text);
  public static native String getContent(long wordHandle);
  public static native String getDictName(long wordHandle);
  public static native boolean activeDict(long dictHandle);
  public static native boolean unactiveDict(long dictHandle);
}
