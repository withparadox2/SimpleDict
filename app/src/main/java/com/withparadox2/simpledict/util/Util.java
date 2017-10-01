package com.withparadox2.simpledict.util;

import android.widget.Toast;
import com.withparadox2.simpledict.DictApp;

/**
 * Created by withparadox2 on 2017/8/27.
 */

public class Util {
  public static void toast(final String text) {
    Runnable action = new Runnable() {
      @Override public void run() {
        Toast.makeText(DictApp.getInstance(), text, Toast.LENGTH_SHORT).show();
      }
    };
    DictApp.getInstance().run(action);
  }
}
