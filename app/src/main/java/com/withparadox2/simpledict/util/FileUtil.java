package com.withparadox2.simpledict.util;

import android.os.Environment;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/**
 * Created by withparadox2 on 2017/8/24.
 */

public class FileUtil {
    public static final String DICT_DIR = "simpledict";

    public static File fromPath(String path) {
        File extStore = Environment.getExternalStorageDirectory();
        return new File(extStore.getPath(), path);
    }

    public static File fromAppPath(String path) {
        return fromPath(DICT_DIR + "/" + path);
    }

    public static void closeQuietly(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
