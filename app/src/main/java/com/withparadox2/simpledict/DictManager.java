package com.withparadox2.simpledict;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by withparadox2 on 2017/8/24.
 */

public class DictManager {
  public static List<Dict> sDictList = new ArrayList<>();

  public static boolean isInstalled(File ld2File) {
    return new File(ld2File.getParent(), ld2File.getName() + ".inflated").exists();
  }

  public static void installAndPrepareAll() {
    new Thread(new Runnable() {
      @Override public void run() {
        sDictList.clear();
        File dir = FileUtil.fromPath(FileUtil.DICT_DIR);
        if (!dir.exists()) {
          return;
        }

        final File[] files = dir.listFiles(new FilenameFilter() {
          @Override public boolean accept(File dir, String name) {
            return name.lastIndexOf("ld2") == name.length() - 3;
          }
        });

        if (files == null) {
          Util.toast("Check dicts or permission.");
          return;
        }

        for (File file : files) {
          Dict dict = new Dict(file);
          if (!isInstalled(file)) {
            NativeLib.install(file.getAbsolutePath());
          }
          dict.setIsInstalled(true);
          dict.setIsSelected(true);
          dict.setRef(NativeLib.prepare(file.getAbsolutePath()));
          NativeLib.activeDict(dict.getRef());
        }

        Util.toast("Install success.");
      }
    }).start();
  }
}
