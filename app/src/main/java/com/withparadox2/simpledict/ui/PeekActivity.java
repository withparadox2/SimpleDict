package com.withparadox2.simpledict.ui;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.LayoutRes;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import com.withparadox2.simpledict.R;
import com.withparadox2.simpledict.dict.SearchItem;
import com.withparadox2.simpledict.util.Util;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by withparadox2 on 2017/10/8.
 */

public class PeekActivity extends WordDetailActivity {
  private Spinner spinner;
  private ArrayAdapter<SearchItem> mAdapter;
  protected List<SearchItem> mShowItemList = new ArrayList<>();

  @Override public void setContentView(@LayoutRes int layoutResID) {
    configWindowSize();
    super.setContentView(layoutResID);
    configDecorViewSize();
    spinner = (Spinner) findViewById(R.id.spinner);
    mAdapter = new ArrayAdapter<>(this, R.layout.item_peek_spinner, mShowItemList);
    mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(mAdapter);
    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mCurItem = mAdapter.getItem(position);
        clearBackStack();
        loadContentIntoWebView();
      }

      @Override public void onNothingSelected(AdapterView<?> parent) {
      }
    });
    findViewById(R.id.btn_speak).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        speak();
      }
    });
  }

  @Override protected int getContentViewId() {
    return R.layout.activity_peek;
  }

  @Override protected void updateIntent() {
    super.updateIntent();
    mAdapter.notifyDataSetChanged();
  }

  @Override protected void loadContentIntoWebView() {
    super.loadContentIntoWebView();
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

  public static Intent getIntent(Context context, List<SearchItem> items) {
    Intent intent = new Intent(context, PeekActivity.class);
    intent.putExtra(WordDetailActivity.KEY_SEARCH_ITEMS, (Serializable) items);
    return intent;
  }

  @Override protected void onUpdateItemList() {
    super.onUpdateItemList();
    mShowItemList.clear();
    mShowItemList.addAll(mItemList);
    mAdapter.notifyDataSetChanged();
  }

  @Override public boolean isFullScreen() {
    return false;
  }
}
