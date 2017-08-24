package com.withparadox2.simpledict;

import android.os.Environment;
import java.io.File;

/**
 * Created by gsd on 2017/8/24.
 */

public class FileUtil {
    public static final String DICT_DIR = "simpledict";

    public static File fromPath(String path) {
        File extStore = Environment.getExternalStorageDirectory();
        return new File(extStore.getPath(), path);
    }
}
