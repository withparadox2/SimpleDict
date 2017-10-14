package com.withparadox2.simpledict.util;

import android.content.Context;
import android.widget.Toast;
import com.withparadox2.simpledict.DictApp;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by withparadox2 on 2017/8/27.
 */

public class Util {
  public static final String MD5_SEQ = "0123456789ABCDEF";

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

  public static String cleanWord(String word) {
    word = word.toLowerCase();
    return word.replaceAll("\\W*$", "").replaceAll("^\\W*", "");
  }

  public static String getRealWord(String word) {
    if (word.endsWith("ing")) {
      return word.replaceAll("ing$", "");
    } else if (word.endsWith("ies")) {
      return word.replaceAll("ies$", "y");
    } else if (word.endsWith("es")) {
      return word.replaceAll("es$", "");
    } else if (word.endsWith("s")) {
      return word.replaceAll("s$", "");
    } else if (word.endsWith("ied")) {
      return word.replaceAll("ied$", "y");
    } else if (word.endsWith("ed")) {
      int len = word.length();
      if (len >= 4 && word.charAt(len - 3) == word.charAt(len - 4)) {
        return word.substring(0, len - 3);
      } else {
        return word.substring(0, len - 2);
      }
    } else if (word.endsWith("'s")) {
      return word.substring(0, word.length() - 2);
    }
    return word;
  }

  public static String getDictId(String fileName) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] bytes = md.digest(fileName.getBytes("UTF-8"));
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < 4; i++) {
        sb.append(MD5_SEQ.charAt((bytes[i] & 0xF0) >> 4));
        sb.append(MD5_SEQ.charAt((bytes[i] & 0x0F)));
      }
      return sb.toString();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return fileName;
  }
}
