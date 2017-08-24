package com.withparadox2.simpledict;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gsd on 2017/8/24.
 */

public class SettingActivity extends Activity {
    private ListView mListView;
    private DictAdapter mAdapter;
    private List<Dict> mDicts = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mListView = (ListView) findViewById(R.id.list_view);
        mAdapter = new DictAdapter();
        mListView.setAdapter(mAdapter);

        loadDicts();
    }

    private void loadDicts() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File dir = FileUtil.fromPath(FileUtil.DICT_DIR);
                if (!dir.exists()) {
                    return;
                }

                final File[] files = dir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.lastIndexOf("ld2") > 0;
                    }
                });

                SettingActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (File file : files) {
                            Dict dict = new Dict(file);
                            dict.setIsInstalled(DictManager.isInstalled(file));
                            dict.setIsSelected(false);
                            mDicts.add(new Dict(file));
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    class DictAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mDicts.size();
        }

        @Override
        public Dict getItem(int position) {
            return mDicts.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                view = new TextView(SettingActivity.this);
                view.setPadding(20, 15, 20, 15);
            }
            TextView tv = (TextView) view;
            tv.setText(getItem(position).getName());
            return view;
        }
    }
}
