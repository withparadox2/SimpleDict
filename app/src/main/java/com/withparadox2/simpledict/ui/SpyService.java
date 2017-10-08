package com.withparadox2.simpledict.ui;

import android.app.Notification;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import com.withparadox2.simpledict.NativeLib;
import com.withparadox2.simpledict.R;
import com.withparadox2.simpledict.dict.SearchItem;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by withparadox2 on 2017/10/3.
 */

public class SpyService extends Service {
  private ClipboardManager.OnPrimaryClipChangedListener mPrimaryChangeListener =
      new ClipboardManager.OnPrimaryClipChangedListener() {
        @Override public void onPrimaryClipChanged() {
          ClipboardManager clipboard =
              (ClipboardManager) SpyService.this.getSystemService(Context.CLIPBOARD_SERVICE);
          final ClipData clipdata = clipboard.getPrimaryClip();
          if (clipdata.getItemCount() > 0) {
            String text = clipdata.getItemAt(0).getText().toString();
            text = cleanWord(text);
            List<SearchItem> wordList = NativeLib.search(text);
            if (wordList.size() == 0 || !TextUtils.equals(text, wordList.get(0).text)) {
              text = getRealWord(text);
              wordList = NativeLib.search(text);
            }
            if (wordList.size() > 0) {
              Intent intent = WordDetailActivity.getIntent(SpyService.this, wordList.get(0));
              intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
              startActivity(intent);
            }
          }
        }
      };

  private String cleanWord(String word) {
    word = word.toLowerCase();
    return word.replaceAll("\\W*$", "");
  }

  private String getRealWord(String word) {
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

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }

  @Override public void onCreate() {
    super.onCreate();
    ClipboardManager clipboard =
        (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);

    clipboard.addPrimaryClipChangedListener(mPrimaryChangeListener);
    Notification notification = new NotificationCompat.Builder(this).setContentTitle("SimpleDict")
        .setSmallIcon(R.mipmap.ic_launcher)
        .build();
    startForeground(1, notification);
  }
}
