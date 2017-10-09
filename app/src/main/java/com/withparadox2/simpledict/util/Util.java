package com.withparadox2.simpledict.util;

import android.content.Context;
import android.widget.Toast;
import com.withparadox2.simpledict.DictApp;

/**
 * Created by withparadox2 on 2017/8/27.
 */

public class Util {
  public static void toast(final String text) {
    post(new Runnable() {
      @Override public void run() {
        Toast.makeText(DictApp.getInstance(), text, Toast.LENGTH_SHORT).show();
      }
    });
  }

  public static void post(Runnable action) {
    DictApp.getInstance().getHandler().post(action);
  }

  public static void postDelayed(Runnable action, long delayMillis) {
    DictApp.getInstance().getHandler().postDelayed(action, delayMillis);
  }

  public static int dp2px(Context context, int dip) {
    float scale = context.getResources().getDisplayMetrics().density;
    return (int) (dip * scale + 0.5f);
  }

  public static int px2dp(Context context, int pixel) {
    float scale = context.getResources().getDisplayMetrics().density;
    return (int) ((pixel - 0.5f) / scale);
  }

  public static int getScreenWidth(Context context) {
    return context.getResources().getDisplayMetrics().widthPixels;
  }

  public static int getScreenHeight(Context context) {
    return context.getResources().getDisplayMetrics().heightPixels;
  }

  public static String stripSuffix(String input) {
    int suffixIndex;
    if ((suffixIndex = input.lastIndexOf('.')) != -1) {
      return input.substring(0, suffixIndex);
    } else {
      return input;
    }
  }
}
