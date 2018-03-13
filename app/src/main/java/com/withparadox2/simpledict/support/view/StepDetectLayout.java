package com.withparadox2.simpledict.support.view;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import com.withparadox2.simpledict.util.Util;

/**
 * Created by withparadox2 on 2018/2/4.
 */
public class StepDetectLayout extends FrameLayout {
  private int mTouchSlop;
  private int mStepSize;
  private Callback mCallback;
  private float mDownX;
  private float mDownY;
  private boolean mAbortDetect;
  private boolean mCounting;
  private int mLastStep;


  public StepDetectLayout(@NonNull Context context) {
    super(context);
    init();
  }

  public StepDetectLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public StepDetectLayout(@NonNull Context context, @Nullable AttributeSet attrs,
      @AttrRes int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    mStepSize = Util.dp2px(getContext(), 10);
    mTouchSlop = Util.dp2px(getContext(), 10);
  }

  public void setCallback(Callback callback) {
    this.mCallback = callback;
  }

  @Override public boolean onInterceptTouchEvent(MotionEvent ev) {
    switch (ev.getAction()) {
      case MotionEvent.ACTION_DOWN: {
        mDownX = ev.getX();
        mDownY = ev.getY();
        mAbortDetect = false;
        mCounting = false;
        mLastStep = -1;
        break;
      }
      case MotionEvent.ACTION_MOVE: {
        if (mAbortDetect) {
          break;
        }
        float x = ev.getX();
        float y = ev.getY();

        float yMoveVal = Math.abs(y - mDownY);
        float xMoveVal = Math.abs(x - mDownX);
        if (xMoveVal > mStepSize) {
          if (yMoveVal > mTouchSlop && !mCounting) {
            mAbortDetect = true;
          } else {
            boolean isNewFire = !mCounting;
            mCounting = true;
            int step = (int) (Math.abs(x - mDownX) / mStepSize);
            if (step != mLastStep) {
              mLastStep = step;
              fireStep(step, isNewFire);
            }
          }
        }
        break;
      }
      case MotionEvent.ACTION_CANCEL:
      case MotionEvent.ACTION_UP:
        if (mCounting) {
          cancelStep();
        }
        break;
    }
    return super.onInterceptTouchEvent(ev);
  }

  // step: Starts from 1
  // newFire: Indicates callee to reset state
  private void fireStep(int step, boolean newFire) {
    if (mCallback != null) {
      mCallback.onStepFire(step, newFire);
    }
  }

  private void cancelStep() {
    if (mCallback != null) {
      mCallback.onCancel();
    }
  }

  public interface Callback {
    void onStepFire(int step, boolean newFire);

    void onCancel();
  }
}
