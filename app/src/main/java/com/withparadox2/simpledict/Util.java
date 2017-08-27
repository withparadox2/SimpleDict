package com.withparadox2.simpledict;

import android.widget.Toast;

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
