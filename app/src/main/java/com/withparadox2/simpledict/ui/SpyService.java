package com.withparadox2.simpledict.ui;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
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
            text = Util.cleanWord(text);
            List<SearchItem> wordList = NativeLib.search(text);
            if (wordList.size() == 0) {
              text = Util.getRealWord(text);
              wordList = NativeLib.search(text);
            }
            if (wordList.size() > 0) {
              Intent intent = PeekActivity.getIntent(SpyService.this, wordList);
              intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
              intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
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

    Intent resultIntent = new Intent(this, HomeActivity.class);
    resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
    resultIntent.setAction(Intent.ACTION_MAIN);
    PendingIntent resultPendingIntent =
        PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    Notification notification = new NotificationCompat.Builder(this).setContentTitle("SimpleDict")
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentIntent(resultPendingIntent)
        .build();
    startForeground(1, notification);
  }
}
