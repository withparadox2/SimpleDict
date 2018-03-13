package com.withparadox2.simpledict.ui;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;

import com.withparadox2.simpledict.support.permission.PermissionManager;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

/**
 * Created by withparadox2 on 2017/10/1.
 */

public class BaseActivity extends SwipeBackActivity {

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setFullScreen();
    if (showBackButton() && getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        this.finish();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  public boolean showBackButton()  {
    return true;
  }

  public void setTitle(String title) {
    if (getSupportActionBar() != null) {
      getSupportActionBar().setTitle(title);
    }
  }

  private void setFullScreen() {
    if (isFullScreen()) {
      getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
  }

  public boolean isFullScreen() {
    return false;
  }


  public void makeActionBarTransparent() {
    if (getSupportActionBar() != null) {
      getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
      getSupportActionBar().setStackedBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    PermissionManager.handlePermissionResult(requestCode, permissions, grantResults);
  }
}
