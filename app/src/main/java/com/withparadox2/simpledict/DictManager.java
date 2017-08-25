package com.withparadox2.simpledict;

import java.io.File;

/**
 * Created by withparadox2 on 2017/8/24.
 */

public class DictManager {
    public static boolean isInstalled(File ld2File) {
        return new File(ld2File.getParent(), ld2File.getName() + ".inflated").exists();
    }
}
