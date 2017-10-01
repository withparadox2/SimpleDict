package com.withparadox2.simpledict;

import android.app.Application;
import android.os.Handler;
import android.os.Message;
import com.withparadox2.simpledict.dict.Dict;

/**
 * Created by withparadox2 on 2017/8/25.
 */

public class DictApp extends Application {
  private static DictApp sInstance;
  private Dict activeDict;
  private Handler mHandler = new Handler() {
    @Override public void handleMessage(Message msg) {
      super.handleMessage(msg);
    }
  };

  @Override public void onCreate() {
    super.onCreate();
    sInstance = this;
  }

  public static DictApp getInstance() {
    return sInstance;
  }

  public Dict getActiveDict() {
    return this.activeDict;
  }

  public void setActiveDict(Dict dict) {
    this.activeDict = dict;
  }

  public void run(Runnable action) {
    mHandler.post(action);
  }
}
