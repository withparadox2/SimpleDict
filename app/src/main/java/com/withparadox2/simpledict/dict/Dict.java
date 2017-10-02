package com.withparadox2.simpledict.dict;

import com.withparadox2.simpledict.NativeLib;
import java.io.File;

/**
 * Created by withparadox2 on 2017/8/24.
 */

public class Dict {
    private long ref;
    private String name;
    private File file;
    private boolean isInstalled;
    private boolean isActive;

    public Dict(String name, File file) {
        this.name = name;
        this.file = file;
    }

    public Dict(File file) {
        String fileName = file.getName();
        int suffixIndex;
        if ((suffixIndex = fileName.lastIndexOf('.')) != -1) {
            this.name = fileName.substring(0, suffixIndex);
        } else {
            this.name = fileName;
        }
        this.file = file;
    }

    public boolean equals(Dict obj) {
        if (obj == null) {
            return false;
        }
        return this.name.equals(obj.name);
    }

    public void setIsInstalled(boolean isInstalled) {
        this.isInstalled = isInstalled;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public String getName() {
        return name;
    }

    public File getFile() {
        return file;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isInstalled() {
        return isInstalled;
    }

    public long getRef() {
        return ref;
    }

    public void setRef(long ref) {
        this.ref = ref;
    }

    public boolean isReady() {
        return ref != 0;
    }

    public void prepare() {
        ref = NativeLib.prepare(file.getPath());
    }

    public Dict copy() {
        Dict d = new Dict(name, file);
        d.ref = this.ref;
        d.isInstalled = this.isInstalled;
        d.isActive = this.isActive;
        return d;
    }
}
