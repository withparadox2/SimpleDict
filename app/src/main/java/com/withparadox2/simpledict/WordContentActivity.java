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
  public static final String KEY_WORD = "word";

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_wod_content);
    tvContent = (TextView) findViewById(R.id.tv_content);
    tvWord = (TextView) findViewById(R.id.tv_word);

    Word word = (Word) getIntent().getSerializableExtra(KEY_WORD);
    tvWord.setText(word.text);
    tvContent.setText(formatContent(NativeLib.getContent(word.ref)));
  }

  private String formatContent(String text) {
    return text.replace("<C><F><H /><I><N>", "")
        .replace("</N></I></F></C>", "")
        .replace("<n /> ", "\n")
        .replace("<n />", "\n");
  }
}
