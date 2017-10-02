package com.withparadox2.simpledict.dict;

import android.text.TextUtils;
import com.withparadox2.simpledict.NativeLib;
import com.withparadox2.simpledict.util.FileUtil;
import com.withparadox2.simpledict.util.PreferencesUtil;
import com.withparadox2.simpledict.util.Util;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by withparadox2 on 2017/8/24.
 */

public class DictManager {
  public static List<Dict> sDictList = new ArrayList<>();
  private static final String KEY_ACTIVE_DICTS = "active_dicts";

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

        Set<String> activeSet = getActiveDictSet();

        for (File file : files) {
          Dict dict = new Dict(file);
          if (!isInstalled(file)) {
            NativeLib.install(file.getAbsolutePath());
          }
          dict.setIsInstalled(true);
          if (activeSet.contains(dict.getName())) {
            dict.setIsActive(true);
            dict.setRef(NativeLib.prepare(file.getAbsolutePath()));
            NativeLib.activeDict(dict.getRef());
          }
          sDictList.add(dict);
        }

        Util.toast("Install success.");
      }
    }).start();
  }

  public static void activeDict(Dict dict) {
    dict.setIsActive(true);
    if (!dict.isReady()) {
      dict.setRef(NativeLib.prepare(dict.getFile().getAbsolutePath()));
      NativeLib.activeDict(dict.getRef());
    }
  }

  private static Set<String> getActiveDictSet() {
    String dictStr = PreferencesUtil.getString(KEY_ACTIVE_DICTS, "");
    if (!TextUtils.isEmpty(dictStr)) {
      String[] dicts = dictStr.split("###");
      return new HashSet<>(Arrays.asList(dicts));
    }
    return new HashSet<String>(0);
  }

  public static void saveActiveDicts(List<Dict> dicts) {
    StringBuilder sb = new StringBuilder();
    for (Dict dict : dicts) {
      if (dict.isActive()) {
        if (sb.length() != 0) {
          sb.append("###");
        }
        sb.append(dict.getName());
      }
    }
    PreferencesUtil.putString(KEY_ACTIVE_DICTS, sb.toString());
  }
}
