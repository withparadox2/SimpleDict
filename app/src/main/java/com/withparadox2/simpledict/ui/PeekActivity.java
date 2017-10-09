package com.withparadox2.simpledict.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;
import com.withparadox2.simpledict.R;
import com.withparadox2.simpledict.dict.SearchItem;
import com.withparadox2.simpledict.util.Util;

/**
 * Created by withparadox2 on 2017/10/8.
 */

public class PeekActivity extends WordDetailActivity {

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    configWindowSize();
    super.onCreate(savedInstanceState);
    configDecorViewSize();

    TextView wordTitle = (TextView) findViewById(R.id.tv_word_name);
    wordTitle.setText(mSearchItem.text);
  }

  @Override protected int getContentViewId() {
    return R.layout.activity_peek;
  }

  private void configWindowSize() {
    android.view.WindowManager.LayoutParams lp = getWindow().getAttributes();
    lp.width = getDialogWidth();
    lp.height = getDialogHeight();
    lp.gravity = Gravity.TOP;
    getWindow().setAttributes(lp);
  }

  private void configDecorViewSize() {
    ViewGroup.LayoutParams lp = findViewById(android.R.id.content).getLayoutParams();
    int padding = getResources().getDimensionPixelOffset(R.dimen.peek_dialog_padding);
    lp.width = getDialogWidth() - 2 * padding;
    lp.height = getDialogHeight();
  }

  private int getDialogWidth() {
    return Util.getScreenWidth(this);
  }

  private int getDialogHeight() {
    return Util.getScreenHeight(this) / 3 * 2;
  }

  public static Intent getIntent(Context context, SearchItem item) {
    Intent intent = new Intent(context, PeekActivity.class);
    intent.putExtra(WordDetailActivity.KEY_SEARCH_ITEM, item);
    return intent;
  }
}
