package com.withparadox2.simpledict.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
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
    final BaseAdapter adapter = new WordListAdapter();
    lv.setAdapter(adapter);

    EditText editText = (EditText) findViewById(R.id.et_search);
    editText.addTextChangedListener(new TextWatcher() {
      @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        wordList = NativeLib.search(charSequence.toString());
        adapter.notifyDataSetChanged();
      }

      @Override public void afterTextChanged(Editable editable) {
        //Fast to clear edit_text with typing two spaces
        int len = editable.length();
        if (len > 1) {
          if (editable.charAt(len - 1) == ' ' && editable.charAt(len - 2) == ' ') {
            editable.clear();
          }
        }
      }
    });

    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        SearchItem searchItem = (SearchItem) view.getTag();
        Intent intent = new Intent(HomeActivity.this, WordDetailActivity.class);
        intent.putExtra(WordDetailActivity.KEY_SEARCH_ITEM, searchItem);
        startActivity(intent);
      }
    });
  }

  private class WordListAdapter extends BaseAdapter {

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

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.home, menu);
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_setting:
        startActivity(new Intent(this, SettingActivity.class));
        return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
