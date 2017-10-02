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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by withparadox2 on 2017/8/24.
 */

public class SettingActivity extends BaseActivity {
  private DictAdapter mAdapter;
  private List<DictWrapper> mDictList = convert(DictManager.sDictList);
  private int mSrcPosition = -1;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_setting);
    ListView listView = (ListView) findViewById(R.id.list_view);
    mAdapter = new DictAdapter();
    listView.setAdapter(mAdapter);

    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mSrcPosition == -1) {
          mSrcPosition = position;
          mAdapter.getItem(position).isUnderSort = true;
        } else {
          if (mSrcPosition != position) {
            orderItem(mSrcPosition, position);
            mAdapter.notifyDataSetChanged();

          }
          mSrcPosition = -1;
        }
        mAdapter.notifyDataSetChanged();
      }
    });
  }

  private void orderItem(int src, int dest) {
    DictWrapper dict = mDictList.remove(src);
    dict.isUnderSort = false;
    mDictList.add(dest, dict);

    DictManager.sDictList.add(dest, DictManager.sDictList.remove(src));
    DictManager.saveOrders(DictManager.sDictList);
  }

  private static List<DictWrapper> convert(List<Dict> list) {
    List<DictWrapper> wrappers = new ArrayList<>();
    for (Dict dict : list) {
      wrappers.add(new DictWrapper(dict));
    }
    return wrappers;
  }

  private class DictAdapter extends BaseAdapter {

    @Override public int getCount() {
      return mDictList.size();
    }

    @Override public DictWrapper getItem(int position) {
      return mDictList.get(position);
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
      DictWrapper wrapper = getItem(position);
      Dict dict = wrapper.dict;
      viewHolder.tvName.setText(dict.getName());
      viewHolder.checkBox.setChecked(dict.isActive());
      viewHolder.checkBox.setTag(dict);
      viewHolder.checkBox.setOnCheckedChangeListener(mCheckedChangeListener);
      viewHolder.blink(wrapper.isUnderSort);
      return view;
    }
  }

  private CompoundButton.OnCheckedChangeListener mCheckedChangeListener =
      new CompoundButton.OnCheckedChangeListener() {
        @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
          Dict dict = (Dict) buttonView.getTag();
          if (isChecked) {
            DictManager.activateDict(dict);
          } else {
            DictManager.deactivateDict(dict);
          }
          DictManager.saveActiveDicts(DictManager.sDictList);
        }
      };

  private static class ViewHolder {
    TextView tvName;
    CheckBox checkBox;
    View blinkLayout;

    ViewHolder(View view) {
      tvName = (TextView) view.findViewById(R.id.tv_dict_name);
      checkBox = (CheckBox) view.findViewById(R.id.checkbox);
      blinkLayout = view.findViewById(R.id.layout_blink);
    }

    void blink(boolean isBlink) {
      blinkLayout.setVisibility(isBlink ? View.VISIBLE : View.GONE);
    }
  }

  private static class DictWrapper {
    boolean isUnderSort;
    Dict dict;

    DictWrapper(Dict dict) {
      this.dict = dict;
    }
  }
}
