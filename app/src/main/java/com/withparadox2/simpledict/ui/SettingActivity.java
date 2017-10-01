package com.withparadox2.simpledict.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.withparadox2.simpledict.DictApp;
import com.withparadox2.simpledict.NativeLib;
import com.withparadox2.simpledict.R;
import com.withparadox2.simpledict.dict.Dict;
import com.withparadox2.simpledict.dict.DictManager;
import com.withparadox2.simpledict.util.FileUtil;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by withparadox2 on 2017/8/24.
 */

public class SettingActivity extends BaseActivity {
  private ListView mListView;
  private DictAdapter mAdapter;
  private List<Dict> mDicts = new ArrayList<>();

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_setting);
    mListView = (ListView) findViewById(R.id.list_view);
    mAdapter = new DictAdapter();
    mListView.setAdapter(mAdapter);

    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        for (Dict d : mDicts) {
          d.setIsSelected(false);
        }
        Dict dict = mDicts.get(position);
        if (!dict.isReady()) {
          dict.prepare();
        }
        if (dict.isReady()) {
          dict.setIsSelected(true);
          DictApp.getInstance().setActiveDict(dict.copy());
          Toast.makeText(SettingActivity.this, "You can search now.", Toast.LENGTH_SHORT).show();
          mAdapter.notifyDataSetChanged();
        } else {
          Toast.makeText(SettingActivity.this, "Prepare dict failed.", Toast.LENGTH_SHORT).show();
        }
      }
    });
    loadDicts();
  }

  //TODO only load once
  private void loadDicts() {
    new Thread(new Runnable() {
      @Override public void run() {
        File dir = FileUtil.fromPath(FileUtil.DICT_DIR);
        if (!dir.exists()) {
          return;
        }

        final File[] files = dir.listFiles(new FilenameFilter() {
          @Override public boolean accept(File dir, String name) {
            return name.lastIndexOf("ld2") == name.length() - 3;
          }
        });

        if (files == null) {
          SettingActivity.this.runOnUiThread(new Runnable() {
            @Override public void run() {
              Toast.makeText(SettingActivity.this,
                  "Please copy dicts to /sdcard/simpledict or allow permission", Toast.LENGTH_SHORT)
                  .show();
            }
          });
          return;
        }

        SettingActivity.this.runOnUiThread(new Runnable() {
          @Override public void run() {
            Dict activeDict = DictApp.getInstance().getActiveDict();
            for (File file : files) {
              Dict dict = new Dict(file);
              dict.setIsInstalled(DictManager.isInstalled(file));
              dict.setIsSelected(dict.equals(activeDict));
              mDicts.add(dict);
            }
            mAdapter.notifyDataSetChanged();
          }
        });
      }
    }).start();
  }

  class DictAdapter extends BaseAdapter {

    @Override public int getCount() {
      return mDicts.size();
    }

    @Override public Dict getItem(int position) {
      return mDicts.get(position);
    }

    @Override public long getItemId(int position) {
      return 0;
    }

    @Override public View getView(int position, View view, ViewGroup parent) {
      if (view == null) {
        view = LayoutInflater.from(SettingActivity.this)
            .inflate(R.layout.item_dict_manage, parent, false);
        view.setTag(new ViewHolder(view));
      }

      ViewHolder viewHolder = (ViewHolder) view.getTag();
      final Dict dict = getItem(position);
      viewHolder.iconSelected.setVisibility(dict.isSelected() ? View.VISIBLE : View.GONE);
      viewHolder.btnInstall.setVisibility(dict.isInstalled() ? View.GONE : View.VISIBLE);
      viewHolder.tvName.setText(dict.getName());

      viewHolder.btnInstall.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          install(dict);
        }
      });
      return view;
    }
  }

  private void install(final Dict dict) {
    if (dict.isInstalled()) {
      return;
    }

    new Thread(new Runnable() {
      @Override public void run() {
        final int state = NativeLib.install(dict.getFile().getPath());
        SettingActivity.this.runOnUiThread(new Runnable() {
          @Override public void run() {
            if (state == -1) {
              Toast.makeText(SettingActivity.this, "安装失败", Toast.LENGTH_SHORT).show();
            } else {
              dict.setIsInstalled(true);
              Toast.makeText(SettingActivity.this, "安装成功", Toast.LENGTH_SHORT).show();
              mAdapter.notifyDataSetChanged();
            }
          }
        });
      }
    }).start();
  }

  static class ViewHolder {
    View iconSelected;
    TextView tvName;
    Button btnInstall;

    ViewHolder(View view) {
      iconSelected = view.findViewById(R.id.tv_selected);
      tvName = (TextView) view.findViewById(R.id.tv_dict_name);
      btnInstall = (Button) view.findViewById(R.id.btn_install);
    }
  }
}
