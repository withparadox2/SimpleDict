package com.withparadox2.simpledict.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.withparadox2.simpledict.NativeLib;
import com.withparadox2.simpledict.R;
import com.withparadox2.simpledict.dict.DictManager;
import com.withparadox2.simpledict.dict.SearchItem;
import com.withparadox2.simpledict.util.Util;
import java.util.List;

/**
 * Created by withparadox2 on 2017/8/12.
 */

public class HomeActivity extends BaseActivity {
  private List<SearchItem> wordList;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);
    DictManager.installAndPrepareAll();

    ListView lv = (ListView) findViewById(R.id.list_view);
    final BaseAdapter adapter = new MyAdapter();
    lv.setAdapter(adapter);

    findViewById(R.id.tv_setting).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        startActivity(new Intent(HomeActivity.this, SettingActivity.class));
      }
    });

    EditText editText = (EditText) findViewById(R.id.et_search);
    editText.addTextChangedListener(new TextWatcher() {
      @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        wordList = NativeLib.search(charSequence.toString());
        adapter.notifyDataSetChanged();
      }

      @Override public void afterTextChanged(Editable editable) {

      }
    });

    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        SearchItem searchItem = (SearchItem) view.getTag();
        Intent intent = new Intent(HomeActivity.this, WordContentActivity.class);
        intent.putExtra(WordContentActivity.KEY_SEARCH_ITEM, searchItem);
        startActivity(intent);
      }
    });
  }

  class MyAdapter extends BaseAdapter {

    @Override public int getCount() {
      return wordList == null ? 0 : wordList.size();
    }

    @Override public SearchItem getItem(int i) {
      return wordList.get(i);
    }

    @Override public long getItemId(int i) {
      return 0;
    }

    @Override public View getView(int i, View view, ViewGroup viewGroup) {
      if (view == null) {
        view = new TextView(HomeActivity.this);
        int padding = Util.dp2px(HomeActivity.this, 13);
        view.setPadding(padding, padding, padding, padding);
        ((TextView) view).setTextSize(16);
      }
      TextView tv = (TextView) view;
      tv.setText(getItem(i).text);
      tv.setTag(getItem(i));
      return view;
    }
  }

  @Override public boolean showBackButton() {
    return false;
  }
}
