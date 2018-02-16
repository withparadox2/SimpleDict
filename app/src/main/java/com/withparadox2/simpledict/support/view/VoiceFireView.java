package com.withparadox2.simpledict.support.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import com.withparadox2.simpledict.util.Util;

/**
 * Created by withparadox2 on 2018/2/4.
 */

public class VoiceFireView extends View {
  private static final int MAX_STEP_COUNT = 4;

  private static final int STAGE_FIRST_DURATION = 300;
  private static final int STAGE_SECOND_DURATION = 500;


  private int mColor = Color.RED;
  private int mCurrentStep = 0;
  private Paint mPaint;
  private RectF mRect;
  private boolean mIsCancel;

  // < 0 end of anim
  // 0   start  anim
  // > 0 during anim
  private long mAnimTime = 0;

  public VoiceFireView(Context context) {
    super(context);
  }

  public VoiceFireView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public VoiceFireView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override protected void onDraw(Canvas canvas) {
    if (mIsCancel && mAnimTime < 0) {
      return;
    }

    if (mPaint == null) {
      mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
      mRect = new RectF();
    }
    int gap = Util.dp2px(getContext(), 3);
    int radius = Util.dp2px(getContext(), 3);

    int itemHeight = (getHeight() - gap * MAX_STEP_COUNT + gap) / MAX_STEP_COUNT;

    int drawHeight = itemHeight;
    int drawWidth = getWidth();

    //during anim
    if (mAnimTime >= 0) {

      long elapseTime = System.currentTimeMillis() - mAnimTime;

      int minSize = Math.min(drawHeight, drawWidth);

      float rate = elapseTime / (float) STAGE_FIRST_DURATION;

      if (rate < 1) {
        //stage 1

        radius = (int) (radius + (minSize / 2 - radius) * rate);
        if (drawHeight > drawWidth) {
          drawHeight = (int) (drawHeight + (minSize - drawHeight) * rate);
        } else {
          drawWidth = (int) (drawWidth + (minSize - drawWidth) * rate);
        }
      } else {
        //stage 2

        rate = (elapseTime - STAGE_FIRST_DURATION) / (float) STAGE_SECOND_DURATION;
        radius = (int) (minSize / 2 * (1 - rate));

        drawWidth = drawHeight = (int) (minSize * (1 - rate));

        if (rate > 1) {
          stopAnim();
        }
      }
    }

    for (int i = 0; i < MAX_STEP_COUNT; i++) {
      if (i >= mCurrentStep - 1) {
        break;
      }
      int bottom = getHeight() - itemHeight * i - gap * i;
      int top = bottom - itemHeight;
      if (i == MAX_STEP_COUNT - 1) {
        top = 0;
      }
      int alpha = 80 + 170 / MAX_STEP_COUNT * i;
      mPaint.setColor((mColor & 0x00ffffff) | alpha << 24);

      int halfDiffWidth = (getWidth() - drawWidth) / 2;
      int halfDiffHeight = (itemHeight - drawHeight) / 2;

      mRect.set(halfDiffWidth, top + halfDiffHeight, getWidth() - halfDiffWidth,
          bottom - halfDiffHeight);
      canvas.drawRoundRect(mRect, radius, radius, mPaint);
    }
    if (mAnimTime > 0) {
      invalidate();
    }
  }

  public StepDetectLayout.Callback getCallback() {
    return new StepDetectLayout.Callback() {
      @Override public void onStepFire(int step, boolean newFire) {
        if (newFire) {
          mIsCancel = false;
          mColor = randomColor();
          stopAnim();
        }
        mCurrentStep = step;
        invalidate();
      }

      @Override public void onCancel() {
        if (!mIsCancel && mCurrentStep > MAX_STEP_COUNT && mCallback != null) {
          mCallback.onFinalFire();
        }

        mIsCancel = true;
        startAnim();
      }
    };
  }

  private void startAnim() {
    mAnimTime = System.currentTimeMillis();
    invalidate();
  }

  private void stopAnim() {
    mAnimTime = -1;
    mCurrentStep = 0;
  }

  private int randomColor() {
    int val = (int) (0xffffff * Math.random());
    return 0xff000000 | val;
  }

  public interface FireCallback {
    void onFinalFire();
  }

  private FireCallback mCallback;

  public void setFireCallback(FireCallback callback) {
    this.mCallback = callback;
  }
}
