package com.withparadox2.simpledict.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.withparadox2.simpledict.NativeLib;
import com.withparadox2.simpledict.R;
import com.withparadox2.simpledict.dict.SearchItem;
import com.withparadox2.simpledict.dict.Word;
import com.withparadox2.simpledict.support.view.StepDetectLayout;
import com.withparadox2.simpledict.support.view.VoiceFireView;
import com.withparadox2.simpledict.util.Util;
import com.withparadox2.simpledict.voice.Pronounce;
import com.withparadox2.simpledict.voice.VoiceManager;
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
  private List<List<SearchItem>> mBackStack;
  protected Pronounce mPrePronounce;

  @SuppressLint({"AddJavascriptInterface", "SetJavaScriptEnabled"})
  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    makeActionBarTransparent();
    setContentView(getContentViewId());
    webView = (WebView) findViewById(R.id.web_view);
    WebView.setWebContentsDebuggingEnabled(true);
    webView.getSettings().setAllowFileAccess(true);
    webView.getSettings().setJavaScriptEnabled(true);
    webView.setWebViewClient(new MyClient());
    webView.setWebChromeClient(new WebChromeClient());
    webView.addJavascriptInterface(new JSInterface(), "jsi");
    webView.loadUrl("file:///android_asset/main.html");
    updateIntent();
    setUpSpeakerDetect();
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
  }

  @SuppressWarnings("unchecked") protected void updateIntent() {
    List<SearchItem> tempList =
        (List<SearchItem>) getIntent().getSerializableExtra(KEY_SEARCH_ITEMS);
    mItemList.clear();
    mItemList.addAll(tempList);
    clearBackStack();
    onUpdateItemList();
    loadContentIntoWebView();
  }

  protected void onUpdateItemList() {
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
          String dictId = NativeLib.getDictId(word.ref);
          final StringBuilder detail = new StringBuilder();
          detail.append("<div class='word-item'>");
          detail.append("<div class='title-area'><div class='dict-title'>").append(dictName).append("</div><div class='arrow-down'></div></div>");

          detail.append("<div class='word-detail'>")
              .append(formatContent(NativeLib.getContent(word.ref), dictId, word))
              .append("</div>");
          detail.append("</div>");
          synchronized (results) {
            results[curIndex] = detail.toString();
            if (isFull(results)) {

              final StringBuilder sb = new StringBuilder();
              sb.append("<div>");
              for (String result : results) {
                sb.append(result);
              }
              sb.append("</div>");
              Util.post(new Runnable() {
                @Override public void run() {
                  Runnable render = new Runnable() {
                    @Override public void run() {
                      execScript(
                          "window.setContent('" + sb.toString().replaceAll("'", "\\\\'") + "');");
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

  private String formatContent(String text, String dictId, Word word) {
    String base = getExternalStorageDirectory().getAbsolutePath();

    Pattern image = Pattern.compile("<Ë M=\"dict://res/(.*?)\"(.*?)/>");
    Matcher matcher = image.matcher(text);

    text = matcher.replaceAll(
        "<img src=\"file://" + base + "/simpledict/" + dictId + "/$1\" $2></img>");

    matcher.reset();
    String basePath = base + "/simpledict/" + dictId + "/";

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

  private class MyClient extends WebViewClient {
    @Override public void onPageFinished(WebView view, String url) {
      super.onPageFinished(view, url);
      mIsWvLoadFinish = true;
      if (mLoadFinishAction != null) {
        mLoadFinishAction.run();
        mLoadFinishAction = null;
      }
    }
  }

  private class JSInterface {
    @JavascriptInterface public void onClickWord(final String text) {
      Util.toast(text);
      Util.post(new Runnable() {
        @Override public void run() {
          WordDetailActivity.this.onClickWord(Util.cleanWord(text));
        }
      });
    }

    @JavascriptInterface public void toast(String text) {
      Util.toast(text);
    }
  }

  protected void onClickWord(String text) {
    List<SearchItem> wordList = NativeLib.search(text);
    if (wordList.size() == 0) {
      text = Util.getRealWord(text);
      wordList = NativeLib.search(text);
    }
    if (wordList.size() > 0) {
      if (TextUtils.equals(mCurItem.text, wordList.get(0).text)) {
        return;
      }
      if (mBackStack == null) {
        mBackStack = new ArrayList<>();
      }
      mBackStack.add(mItemList);
      mItemList = wordList;
      onUpdateItemList();
      loadContentIntoWebView();
    }
  }

  protected void clearBackStack() {
    if (mBackStack != null) {
      mBackStack.clear();
      execScript("clearBackStack()");
    }
  }

  @Override public void onBackPressed() {
    if (mBackStack != null && mBackStack.size() > 0) {
      mItemList = mBackStack.remove(mBackStack.size() - 1);
      onUpdateItemList();
      execScript("backClick()");
      return;
    }
    super.onBackPressed();
  }

  private void execScript(String jsCode) {
    webView.loadUrl("javascript:" + jsCode);
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.detail, menu);
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_speak:
        speak();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  protected void speak() {
    if (mCurItem != null) {
      if (mPrePronounce != null) {
        mPrePronounce.cancel();
      }
      mPrePronounce = VoiceManager.speak(mCurItem.text);
    }
  }

  public void setUpSpeakerDetect() {
    StepDetectLayout layout = (StepDetectLayout) findViewById(R.id.layout_word_detail);
    if (layout == null) {
      return;
    }

    VoiceFireView view = (VoiceFireView) findViewById(R.id.voice_fire_view);
    if (view == null) {
      return;
    }

    layout.setCallback(view.getCallback());
    view.setFireCallback(new VoiceFireView.FireCallback() {
      @Override public void onFinalFire() {
        speak();
      }
    });
  }

  @Override public boolean isFullScreen() {
    return true;
  }
}
