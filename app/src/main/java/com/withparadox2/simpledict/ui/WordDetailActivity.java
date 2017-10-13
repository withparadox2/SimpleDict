package com.withparadox2.simpledict.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.withparadox2.simpledict.NativeLib;
import com.withparadox2.simpledict.R;
import com.withparadox2.simpledict.dict.SearchItem;
import com.withparadox2.simpledict.dict.Word;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.os.Environment.getExternalStorageDirectory;

/**
 * Created by withparadox2 on 2017/8/22.
 */

public class WordDetailActivity extends BaseActivity {
  protected WebView webView;
  public static final String KEY_SEARCH_ITEMS = "search_items";
  protected SearchItem mCurItem;
  protected List<SearchItem> mItemList = new ArrayList<>();
  private ExecutorService mExecutor;
  private boolean mIsWvLoadFinish = false;
  private Runnable mLoadFinishAction;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(getContentViewId());
    webView = (WebView) findViewById(R.id.web_view);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      WebView.setWebContentsDebuggingEnabled(true);
    }
    webView.getSettings().setAllowFileAccess(true);
    webView.getSettings().setJavaScriptEnabled(true);
    webView.setWebViewClient(new MyClient());
    webView.setWebChromeClient(new WebChromeClient());
    webView.loadUrl("file:///android_asset/main.html");
    updateIntent();
    loadContentIntoWebView();
  }

  private void initExecutor(int dictCount) {
    int threadCount = Math.min(dictCount, 4);
    if (mExecutor == null) {
      mExecutor = new ThreadPoolExecutor(threadCount, threadCount, 0, TimeUnit.MILLISECONDS,
          new LinkedBlockingDeque<Runnable>());
    }
  }

  @Override protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
    updateIntent();
    loadContentIntoWebView();
  }

  @SuppressWarnings("unchecked") protected void updateIntent() {
    List<SearchItem> tempList =
        (List<SearchItem>) getIntent().getSerializableExtra(KEY_SEARCH_ITEMS);
    mItemList.clear();
    mItemList.addAll(tempList);
    mCurItem = mItemList.get(0);
    setTitle(mCurItem.text);
  }

  protected void loadContentIntoWebView() {
    List<Word> wordList = mCurItem.wordList;
    initExecutor(wordList.size());

    final String results[] = new String[wordList.size()];
    for (int i = 0; i < wordList.size(); i++) {
      final Word word = wordList.get(i);
      final int curIndex = i;

      mExecutor.execute(new Runnable() {
        @Override public void run() {
          String dictName = NativeLib.getDictName(word.ref);
          final StringBuilder detail = new StringBuilder();
          detail.append("<div class='dict-title'>")
              .append(dictName)
              .append("</div>");

          detail.append("<div class='word-detail'>")
              .append(formatContent(NativeLib.getContent(word.ref), dictName, word))
              .append("</div>");
          synchronized (results) {
            results[curIndex] = detail.toString();
            if (isFull(results)) {

              final StringBuilder sb = new StringBuilder();
              for (String result : results) {
                sb.append(result);
              }
              WordDetailActivity.this.runOnUiThread(new Runnable() {
                @Override public void run() {
                  Runnable render = new Runnable() {
                    @Override public void run() {
                      webView.loadUrl("javascript:window.setContent('"
                          + sb.toString().replaceAll("'", "\\\\'")
                          + "');");
                    }
                  };
                  if (mIsWvLoadFinish) {
                    render.run();
                  } else {
                    mLoadFinishAction = render;
                  }
                }
              });
            }
          }
        }
      });
    }
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    if (mExecutor != null) {
      mExecutor.shutdownNow();
    }
  }

  private boolean isFull(String[] datas) {
    for (int i = 0; i < datas.length; i++) {
      if (datas[i] == null) {
        return false;
      }
    }
    return true;
  }

  protected int getContentViewId() {
    return R.layout.activity_wod_content;
  }

  private String formatContent(String text, String dictName, Word word) {
    String base = getExternalStorageDirectory().getAbsolutePath();

    Pattern image = Pattern.compile("<Ë M=\"dict://res/(.*?)\"(.*?)/>");
    Matcher matcher = image.matcher(text);

    text = matcher.replaceAll("<img src=\"file://"
        + base
        + "/simpledict/"
        + dictName
        + "/$1\" $2></img>");

    matcher.reset();
    String basePath = base + "/simpledict/" + dictName + "/";

    StringBuilder sb = new StringBuilder();
    while (matcher.find()) {
      String picName = matcher.group(1);
      if (!new File(basePath + picName).exists()) {
        if (sb.length() != 0) {
          sb.append(";");
        }
        sb.append(picName);
      }
    }
    if (sb.length() > 0) {
      new File(basePath).mkdirs();
      NativeLib.loadRes(word.ref, sb.toString());
    }

    Pattern strong = Pattern.compile("<g>(.*?)</g>");
    matcher = strong.matcher(text);
    text = matcher.replaceAll("<b>$1</b>").replaceAll("<n />", "<br>");

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
    List<SearchItem> items = new ArrayList<>();
    items.add(item);
    intent.putExtra(WordDetailActivity.KEY_SEARCH_ITEMS, (Serializable) items);
    return intent;
  }

  class MyClient extends WebViewClient {
    @Override public void onPageFinished(WebView view, String url) {
      super.onPageFinished(view, url);
      mIsWvLoadFinish = true;
      if (mLoadFinishAction != null) {
        mLoadFinishAction.run();
        mLoadFinishAction = null;
      }
    }
  }
}
