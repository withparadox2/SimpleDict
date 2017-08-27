package com.withparadox2.simpledict;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

/**
 * Created by withparadox2 on 2017/8/22.
 */

public class WordContentActivity extends Activity {
  private TextView tvWord;
  private TextView tvContent;
  public static final String KEY_SEARCH_ITEM = "search_item";

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_wod_content);
    tvContent = (TextView) findViewById(R.id.tv_content);
    tvWord = (TextView) findViewById(R.id.tv_word);

    SearchItem searchItem = (SearchItem) getIntent().getSerializableExtra(KEY_SEARCH_ITEM);
    tvWord.setText(searchItem.text);

    StringBuilder sb = new StringBuilder();
    for (Word word : searchItem.wordList) {
      sb.append(NativeLib.getDictName(word.ref)).append("\n\n");
      sb.append(formatContent(NativeLib.getContent(word.ref))).append("\n\n\n");
    }
    tvContent.setText(sb.toString());
  }

  private String formatContent(String text) {
    return text.replace("<C><F><H /><I><N>", "")
        .replace("</N></I></F></C>", "")
        .replace("<n /> ", "\n")
        .replace("<n />", "\n");
  }
}
