package com.withparadox2.simpledict;

import android.app.Application;

/**
 * Created by withparadox2 on 2017/8/25.
 */

public class DictApp extends Application {
    private static DictApp sInstance;
    private Dict activeDict;
    @Override
    public void onCreate() {
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
}
