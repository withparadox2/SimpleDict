package com.withparadox2.simpledict;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
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
import java.io.File;
import java.util.List;

/**
 * Created by withparadox2 on 2017/8/12.
 */

public class HomeActivity extends Activity {
  private List<Word> wordList;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);

    File extStore = Environment.getExternalStorageDirectory();
    String file = new File(extStore.getPath(), "Collins.ld2").toString();

    final long dict = NativeLib.prepare(file);

    ListView lv = (ListView) findViewById(R.id.list_view);
    final BaseAdapter adapter = new MyAdapter();
    lv.setAdapter(adapter);

    findViewById(R.id.tv_setting).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(HomeActivity.this, SettingActivity.class));
      }
    });

    EditText editText = (EditText) findViewById(R.id.et_search);
    editText.addTextChangedListener(new TextWatcher() {
      @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        wordList = NativeLib.search(dict, charSequence.toString());
        adapter.notifyDataSetChanged();
      }

      @Override public void afterTextChanged(Editable editable) {

      }
    });

    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Word word = (Word) view.getTag();
        Intent intent = new Intent(HomeActivity.this, WordContentActivity.class);
        intent.putExtra(WordContentActivity.KEY_WORD, word);
        startActivity(intent);
      }
    });
  }

  class MyAdapter extends BaseAdapter {

    @Override public int getCount() {
      return wordList == null ? 0 : wordList.size();
    }

    @Override public Word getItem(int i) {
      return wordList.get(i);
    }

    @Override public long getItemId(int i) {
      return 0;
    }

    @Override public View getView(int i, View view, ViewGroup viewGroup) {
      if (view == null) {
        view = new TextView(HomeActivity.this);
        view.setPadding(20, 15, 20, 15);
      }
      TextView tv = (TextView) view;
      tv.setText(getItem(i).text);
      tv.setTag(getItem(i));
      return view;
    }
  }
}
