package com.withparadox2.simpledict.util;

import android.content.Context;
import android.util.TypedValue;
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


  public static int dp2px(Context context, int dip) {
    float scale = context.getResources().getDisplayMetrics().density;
    return (int) (dip * scale + 0.5f);
  }

  public static int px2dp(Context context, int pixel) {
    float scale = context.getResources().getDisplayMetrics().density;
    return (int) ((pixel - 0.5f) / scale);
  }
}
