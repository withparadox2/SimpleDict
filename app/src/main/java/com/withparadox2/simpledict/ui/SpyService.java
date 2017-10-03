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
import com.withparadox2.simpledict.DictApp;
import com.withparadox2.simpledict.NativeLib;
import com.withparadox2.simpledict.R;
import com.withparadox2.simpledict.dict.SearchItem;
import com.withparadox2.simpledict.util.Util;
import java.util.List;

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
            text = text.toLowerCase();
            List<SearchItem> wordList = NativeLib.search(text);
            if (wordList.size() > 0 && TextUtils.equals(wordList.get(0).text, text)) {
              Intent intent = WordDetailActivity.getIntent(SpyService.this, wordList.get(0));
              intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
              startActivity(intent);
            }
          }
        }
      };

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
