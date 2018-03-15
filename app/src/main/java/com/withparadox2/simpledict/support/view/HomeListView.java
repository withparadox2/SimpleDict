package com.withparadox2.simpledict.support.view;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;

import com.withparadox2.simpledict.util.Util;

/**
 * Created by withparadox2 on 2018/3/15.
 */

public class HomeListView extends ListView {
  private float mLastY = 0;
  private float mLastHideY = 0;
  private View mEditText;

  public HomeListView(@NonNull Context context) {
    super(context);
  }

  public HomeListView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public HomeListView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override public boolean onTouchEvent(MotionEvent event) {
    switch (event.getAction()) {
      case MotionEvent.ACTION_MOVE:
        int criticalDistance = Util.dp2px(getContext(), 40);
        if (isReachEnd()) {
          if (mLastY == 0) {
            mLastY = event.getY();
          } else {
            if (mLastY - event.getY() > criticalDistance) {
              showKeyBoard();
            }
          }
        }
        if (mLastHideY - event.getY() < -criticalDistance) {
          hideKeyBoard();
        }
        break;
      case MotionEvent.ACTION_DOWN:
        mLastY = 0;
        mLastHideY = event.getY();
        break;
    }
    return super.onTouchEvent(event);
  }

  private boolean isReachEnd() {
    if (getChildCount() <= 0) {
      return true;
    }

    View lastChild = getChildAt(getChildCount() - 1);
    return lastChild.getBottom() <= getHeight();
  }

  public void setEditText(View view) {
    mEditText = view;
  }

  private void showKeyBoard() {
    if (mEditText != null) {
      mEditText.requestFocus();
    }
    InputMethodManager inputManager =
        (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    inputManager.showSoftInput(mEditText, 0);
  }

  private void hideKeyBoard() {
    InputMethodManager imm =
        (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(getWindowToken(), 0);
  }
}
