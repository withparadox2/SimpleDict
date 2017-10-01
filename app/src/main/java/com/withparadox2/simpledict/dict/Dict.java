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
    private boolean isSelected;

    public Dict(String name, File file) {
        this.name = name;
        this.file = file;
    }

    public Dict(File file) {
        this.name = file.getName();
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

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public String getName() {
        return name;
    }

    public File getFile() {
        return file;
    }

    public boolean isSelected() {
        return isSelected;
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
        d.isSelected = this.isSelected;
        return d;
    }
}
