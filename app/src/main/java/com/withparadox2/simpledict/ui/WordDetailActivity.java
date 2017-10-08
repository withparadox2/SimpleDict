package com.withparadox2.simpledict.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.webkit.WebView;
import com.withparadox2.simpledict.NativeLib;
import com.withparadox2.simpledict.R;
import com.withparadox2.simpledict.dict.SearchItem;
import com.withparadox2.simpledict.dict.Word;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.os.Environment.getExternalStorageDirectory;

/**
 * Created by withparadox2 on 2017/8/22.
 */

public class WordDetailActivity extends BaseActivity {
  protected WebView webView;
  public static final String KEY_SEARCH_ITEM = "search_item";

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_wod_content);
    webView = (WebView) findViewById(R.id.web_view);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      WebView.setWebContentsDebuggingEnabled(true);
    }
    webView.getSettings().setAllowFileAccess(true);
    webView.getSettings().setJavaScriptEnabled(true);

    final SearchItem searchItem = (SearchItem) getIntent().getSerializableExtra(KEY_SEARCH_ITEM);
    setTitle(searchItem.text);

    new Thread(new Runnable() {
      @Override public void run() {
        final StringBuilder sb = new StringBuilder();
        for (Word word : searchItem.wordList) {
          String dictName = NativeLib.getDictName(word.ref);
          sb.append("<div style=\"background:#f2f2f2;padding: 10px;\">")
              .append(dictName)
              .append("</div>");
          sb.append("<div style=\"padding: 8px\">")
              .append(formatContent(NativeLib.getContent(word.ref), dictName))
              .append("</div>");
        }

        WordDetailActivity.this.runOnUiThread(new Runnable() {
          @Override public void run() {
            webView.loadDataWithBaseURL("", "<!DOCTYPE html>\n"
                + "<html>"
                + "<head>"
                + "    <meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">"
                + "</head>"
                + "<body style=\"margin: 0;\">"
                + sb.toString()
                + "</body>"
                + "</html>", "text/html", "UTF-8", null);
          }
        });
      }
    }).start();
  }

  private String formatContent(String text, String dictName) {
    String base = getExternalStorageDirectory().getAbsolutePath();

    Pattern image = Pattern.compile("<Ë M=\"dict://res/(.*?)\" />");
    Matcher matcher = image.matcher(text);

    text = matcher.replaceAll("<img src=\"file://"
        + base
        + "/simpledict/"
        + dictName
        + "/$1\" style=\"max-width: 100%\"></img>").replaceAll("<n />", "<br>");

    Pattern strong = Pattern.compile("<g>(.*?)</g>");
    matcher = strong.matcher(text);
    text = matcher.replaceAll("<b>$1</b>");

    Pattern textStyle = Pattern.compile("<x K=\"(.*?)\">(.*?)</x>");
    matcher = textStyle.matcher(text);
    text = matcher.replaceAll("<font color=\"$1\">$2</font>");

    Pattern div = Pattern.compile("<(/?)[CFIN]>");
    matcher = div.matcher(text);
    text = matcher.replaceAll("<$1div>");

    return text;
  }

  public static Intent getIntent(Context context, SearchItem item) {
    Intent intent = new Intent(context, WordDetailActivity.class);
    intent.putExtra(WordDetailActivity.KEY_SEARCH_ITEM, item);
    return intent;
  }
}
