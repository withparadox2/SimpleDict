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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by withparadox2 on 2017/8/24.
 */

public class DictManager {
  public static final List<Dict> sDictList = new ArrayList<>();
  private static ExecutorService sExecutor;
  private static final String KEY_ACTIVE_DICTS = "active_dicts";

  public static boolean isInstalled(File ld2File) {
    return new File(ld2File.getParent(), ld2File.getName() + ".sd").exists();
  }

  public static void installAndPrepareAll(final Runnable finalAction) {

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

    if (files == null || files.length == 0) {
      Util.toast("Check dicts or permission.");
      return;
    }

    initExecutor(files.length);

    final Set<String> activeSet = getActiveDictSet();

    for (File filePara : files) {
      final File file = filePara;

      sExecutor.execute(new Runnable() {
        @Override public void run() {
          Dict dict = new Dict(file);
          dict.setOrder(getOrder(dict));
          if (!isInstalled(file)) {
            NativeLib.install(file.getAbsolutePath());
          }
          dict.setIsInstalled(true);
          if (activeSet.contains(dict.getId())) {
            activateDict(dict);
          }

          synchronized (sDictList) {
            sDictList.add(dict);
            if (sDictList.size() == files.length) {
              Collections.sort(sDictList);
              Util.toast("Install success.");
              sExecutor.shutdown();
              sExecutor = null;
              if (finalAction != null) {
                Util.post(finalAction);
              }
            }
          }
        }
      });
    }
  }

  private static void initExecutor(int fileCount) {
    int threadCount = Math.min(fileCount, 4);
    if (sExecutor == null) {
      sExecutor = new ThreadPoolExecutor(threadCount, threadCount, 30, TimeUnit.MILLISECONDS,
          new LinkedBlockingDeque<Runnable>());
    }
  }

  public static void activateDict(Dict dict) {
    dict.setIsActive(true);
    if (!dict.isReady()) {
      dict.setRef(NativeLib.prepare(dict.getFile().getAbsolutePath(), dict.getId()));
    }
    NativeLib.activateDict(dict.getRef());
    NativeLib.setDictOrder(dict.getRef(), dict.getOrder());
  }

  public static void deactivateDict(Dict dict) {
    dict.setIsActive(false);
    if (dict.isReady()) {
      NativeLib.deactivateDict(dict.getRef());
    }
  }

  private static Set<String> getActiveDictSet() {
    String dictStr = PreferencesUtil.getString(KEY_ACTIVE_DICTS, "");
    if (!TextUtils.isEmpty(dictStr)) {
      String[] dictIds = dictStr.split("#");
      return new HashSet<>(Arrays.asList(dictIds));
    }
    return new HashSet<>(0);
  }

  public static void saveActiveDicts(List<Dict> dicts) {
    StringBuilder sb = new StringBuilder();
    for (Dict dict : dicts) {
      if (dict.isActive()) {
        if (sb.length() != 0) {
          sb.append("#");
        }
        sb.append(dict.getId());
      }
    }
    PreferencesUtil.putString(KEY_ACTIVE_DICTS, sb.toString());
  }

  public static int getOrder(Dict dict) {
    return PreferencesUtil.getInt(dict.getId() + "-order", -1);
  }

  public static void saveOrders(List<Dict> list) {
    for (int i = 0; i < list.size(); i++) {
      Dict dict = list.get(i);
      dict.setOrder(i);
      PreferencesUtil.putInt(dict.getId() + "-order", i);
    }
  }
}
