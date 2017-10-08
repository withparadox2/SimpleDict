package com.withparadox2.simpledict.ui;

import android.content.Context;
import android.content.Intent;
import com.withparadox2.simpledict.dict.SearchItem;

/**
 * Created by withparadox2 on 2017/10/8.
 */

public class PeekActivity extends WordDetailActivity {
  public static Intent getIntent(Context context, SearchItem item) {
    Intent intent = new Intent(context, PeekActivity.class);
    intent.putExtra(WordDetailActivity.KEY_SEARCH_ITEM, item);
    return intent;
  }
}
