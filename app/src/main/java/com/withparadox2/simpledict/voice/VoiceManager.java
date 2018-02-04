package com.withparadox2.simpledict.voice;

import android.text.TextUtils;
import com.withparadox2.simpledict.util.FileUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by withparadox2 on 2017/10/28.
 */

public class VoiceManager {
  public static Pronounce speak(String word) {
    if (TextUtils.isEmpty(word)) {
      return Pronounce.sDummy;
    }
    File file = FileUtil.fromAppPath("voice/" + word);
    Pronounce p = new Pronounce(word, file);
    if (file.exists()) {
      p.speak();
    } else {
      download(p);
    }
    return p;
  }

  private static void download(final Pronounce p) {
    new Thread(new Runnable() {
      @Override public void run() {
        String url = String.format("http://dict.youdao.com/dictvoice?audio=%s&type=1", p.getWord());
        try {
          HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
          connection.setConnectTimeout(30 * 1000);
          int code = connection.getResponseCode();
          if (code == HttpURLConnection.HTTP_OK) {
            InputStream ifs = connection.getInputStream();
            File f = p.getFile();
            f.getParentFile().mkdirs();
            FileOutputStream ofs = new FileOutputStream(p.getFile());
            int len;
            byte[] buffer = new byte[1024];
            while ((len = ifs.read(buffer)) > 0) {
              ofs.write(buffer, 0, len);
            }

            FileUtil.closeQuietly(ifs);
            FileUtil.closeQuietly(ofs);

            p.speak();
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }).start();
  }
}
