package com.withparadox2.simpledict.voice;

import android.media.AudioManager;
import android.media.SoundPool;
import com.withparadox2.simpledict.util.Util;
import java.io.File;

/**
 * Created by withparadox2 on 2017/10/28.
 */

public class Pronounce {
  private boolean isCancel;
  private String word;
  private File mp3File;

  private static SoundPool sSP = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
  public static Pronounce sDummy = new Pronounce(null, null);

  public Pronounce(String word, File mp3File) {
    this.word = word;
    this.mp3File = mp3File;
  }

  public void cancel() {
    this.isCancel = true;
  }

  public void speak() {
    if (isCancel) {
      return;
    }
    if (mp3File == null || !mp3File.exists()) {
      return;
    }

    final int id = sSP.load(mp3File.getAbsolutePath(), 1);
    sSP.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
      @Override public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
        sSP.play(id, 1, 1, 1, 0, 1);
        sSP.unload(id);
      }
    });
  }

  public String getWord() {
    return word;
  }

  public File getFile() {
    return mp3File;
  }
}
