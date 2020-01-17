package com.withparadox2.simpledict.ui;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import com.withparadox2.simpledict.NativeLib;
import com.withparadox2.simpledict.R;
import com.withparadox2.simpledict.dict.SearchItem;
import com.withparadox2.simpledict.util.Util;
import java.util.List;

/**
 * Created by withparadox2 on 2017/10/3.
 */

public class SpyService extends Service {
  private SearchTask mLastTask;
  private ClipboardManager.OnPrimaryClipChangedListener mPrimaryChangeListener =
      new ClipboardManager.OnPrimaryClipChangedListener() {
        @Override public void onPrimaryClipChanged() {
          ClipboardManager clipboard =
              (ClipboardManager) SpyService.this.getSystemService(Context.CLIPBOARD_SERVICE);
          final ClipData clipdata = clipboard.getPrimaryClip();
          if (clipdata != null && clipdata.getItemCount() > 0) {
            if (clipdata.getItemAt(0).getText() != null) {
              String text = clipdata.getItemAt(0).getText().toString();
              if (mLastTask == null || mLastTask.isDone() || !mLastTask.isSame(text)) {
                mLastTask = new SearchTask(text);
                Util.postDelayed(mLastTask, 10);
              }
            }
          }
        }
      };

  private void searchAggressive(String text) {
    List<SearchItem> wordList = NativeLib.search(text);
    if (wordList.size() == 0) {
      text = Util.getRealWord(text);
      wordList = NativeLib.search(text);
    }
    while (wordList.size() == 0 && text.length() != 0) {
      text = text.substring(0, text.length() - 1);
      wordList = NativeLib.search(text);
    }
    if (wordList.size() > 0) {
      Intent intent = PeekActivity.getIntent(SpyService.this, wordList);
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
      startActivity(intent);
    }
  }

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
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
    Notification notification = new NotificationCompat.Builder(this, createNotificationChannel("spy_service", "spy_service")).setContentTitle("SimpleDict")
        .setLargeIcon(getIconBitmap())
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentIntent(resultPendingIntent)
        .build();
    startForeground(1, notification);
  }

  private Bitmap getIconBitmap() {
    Drawable drawable = getResources().getDrawable(R.drawable.ic_launcher);
    return ((BitmapDrawable) (drawable)).getBitmap();
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  private String createNotificationChannel(String channelId, String channelName) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          NotificationChannel chan = new NotificationChannel(channelId,
                  channelName, NotificationManager.IMPORTANCE_NONE);
          chan.setLightColor(Color.BLUE);
          chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

          NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
          service.createNotificationChannel(chan);
          return channelId;
      } else {
          // If earlier version channel ID is not used
          // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
          return "";
      }
  }

  private class SearchTask implements Runnable {
    private String mWord;
    private boolean mDone = false;

    boolean isSame(String other) {
      return TextUtils.equals(other, mWord);
    }

    boolean isDone() {
      return mDone;
    }

    SearchTask(String word) {
      mWord = word;
    }

    @Override public void run() {
      if (mWord != null) {
        searchAggressive(Util.cleanWord(mWord));
      }
      mDone = true;
    }
  }
}
