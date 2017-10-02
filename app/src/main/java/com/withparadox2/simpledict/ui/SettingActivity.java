package com.withparadox2.simpledict.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import com.withparadox2.simpledict.R;
import com.withparadox2.simpledict.dict.Dict;
import com.withparadox2.simpledict.dict.DictManager;
import java.util.List;

/**
 * Created by withparadox2 on 2017/8/24.
 */

public class SettingActivity extends BaseActivity {
  private DictAdapter mAdapter;
  private List<Dict> mDicts = DictManager.sDictList;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_setting);
    ListView listView = (ListView) findViewById(R.id.list_view);
    mAdapter = new DictAdapter();
    listView.setAdapter(mAdapter);

    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      }
    });
  }


  private class DictAdapter extends BaseAdapter {

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
      viewHolder.tvName.setText(dict.getName());
      viewHolder.checkBox.setChecked(dict.isActive());
      viewHolder.checkBox.setTag(dict);
      viewHolder.checkBox.setOnCheckedChangeListener(mCheckedChangeListenere);
      return view;
    }
  }

  private CompoundButton.OnCheckedChangeListener mCheckedChangeListenere =
      new CompoundButton.OnCheckedChangeListener() {
        @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
          Dict dict = (Dict) buttonView.getTag();
          dict.setIsActive(isChecked);
          if (isChecked) {
            DictManager.activeDict(dict);
          }
          DictManager.saveActiveDicts(mDicts);
        }
      };

  private static class ViewHolder {
    TextView tvName;
    CheckBox checkBox;

    ViewHolder(View view) {
      tvName = (TextView) view.findViewById(R.id.tv_dict_name);
      checkBox = (CheckBox) view.findViewById(R.id.checkbox);
    }
  }

}
