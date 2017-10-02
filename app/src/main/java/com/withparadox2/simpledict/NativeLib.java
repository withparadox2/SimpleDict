package com.withparadox2.simpledict;

import com.withparadox2.simpledict.dict.SearchItem;
import java.io.File;
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

  public static native boolean activateDict(long dictHandle);

  public static native boolean deactivateDict(long dictHandle);

  public static void createFolder(String path) {
    File folder = new File(path);
    if (!folder.exists()) {
      folder.mkdirs();
    }
  }
}
