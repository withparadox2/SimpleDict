package com.withparadox2.simpledict;

import java.io.File;

/**
 * Created by gsd on 2017/8/24.
 */

public class Dict {
    private int ref;
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
}
