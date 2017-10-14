package com.withparadox2.simpledict.dict;

import android.support.annotation.NonNull;
import com.withparadox2.simpledict.NativeLib;
import com.withparadox2.simpledict.util.Util;
import java.io.File;

/**
 * Created by withparadox2 on 2017/8/24.
 */

public class Dict implements Comparable<Dict> {
  private long ref;
  private String name;
  private File file;
  private boolean isInstalled;
  private boolean isActive;
  private int order;
  private String id;

  public Dict(String name, File file) {
    this.name = name;
    this.file = file;
    this.id = Util.getDictId(file.getName());
  }

  public Dict(File file) {
    this(Util.stripSuffix(file.getName()), file);
  }

  public boolean equals(Dict obj) {
    if (obj == null) {
      return false;
    }
    return this.id.equals(obj.getId());
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

  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    this.order = order;
    if (isReady()) {
      NativeLib.setDictOrder(ref, order);
    }
  }

  @Override public int compareTo(@NonNull Dict o) {
    return order - o.order;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
