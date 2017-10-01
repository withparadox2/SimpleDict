package com.withparadox2.simpledict.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.PatternMatcher;
import android.support.annotation.Nullable;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;
import com.withparadox2.simpledict.NativeLib;
import com.withparadox2.simpledict.R;
import com.withparadox2.simpledict.dict.SearchItem;
import com.withparadox2.simpledict.dict.Word;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by withparadox2 on 2017/8/22.
 */

public class WordContentActivity extends Activity {
  private TextView tvWord;
  private WebView webView;
  public static final String KEY_SEARCH_ITEM = "search_item";

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_wod_content);
    tvWord = (TextView) findViewById(R.id.tv_word);
    webView = (WebView) findViewById(R.id.web_view);
    webView.getSettings().setAllowFileAccess(true);
    webView.getSettings().setJavaScriptEnabled(true);

    SearchItem searchItem = (SearchItem) getIntent().getSerializableExtra(KEY_SEARCH_ITEM);
    tvWord.setText(searchItem.text);

    StringBuilder sb = new StringBuilder();
    for (Word word : searchItem.wordList) {
      //sb.append(NativeLib.getDictName(word.ref)).append("\n\n");
      sb.append(formatContent(NativeLib.getContent(word.ref),NativeLib.getDictName(word.ref)));
    }
    String base = Environment.getExternalStorageDirectory().getAbsolutePath().toString();
    String imagePath = "file://"+ base + "/test.gif";

    webView.loadDataWithBaseURL("", "<!DOCTYPE html>\n"
        + "<html>"
        + "<head>"
        + "    <meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">"
        + "</head>"
        + "<body>"
        + sb.toString()
        + "</body>"
        + "</html>", "text/html", "UTF-8", null);
  }

  private String formatContent(String text, String dictName) {
    String base = Environment.getExternalStorageDirectory().getAbsolutePath().toString();

    Pattern image = Pattern.compile("<Ë M=\"dict://res/(.*?)\" />");
    Matcher matcher = image.matcher(text);

    text = matcher.replaceAll("<img src=\"file://" + base + "/simpledict/aaa/$1\" style=\"max-width: 100%\"></img>")
        .replaceAll("<n />", "<br>");

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
}
