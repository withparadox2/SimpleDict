package com.withparadox2.simpledict;

import android.app.Activity;
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
import android.widget.Toast;
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

    long time = System.currentTimeMillis();
    int result = NativeLib.install(file);

    final long dict = NativeLib.prepare(file);

    //List<Word> list = NativeLib.search(dict, "m");
    //StringBuilder sb = new StringBuilder();
    //for (Word w : list) {
    //  sb.append(w.text);
    //  sb.append("\n");
    //}
    //Toast.makeText(this, sb.toString(), Toast.LENGTH_SHORT).show();

    ListView lv = (ListView) findViewById(R.id.list_view);
    final BaseAdapter adapter = new MyAdapter();
    lv.setAdapter(adapter);

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
        String text = NativeLib.getContent(word.ref);
        Toast.makeText(HomeActivity.this, text, Toast.LENGTH_SHORT).show();
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
