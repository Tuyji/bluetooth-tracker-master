package com.visneweb.techbay.tracker.common;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;

import com.visneweb.techbay.tracker.db.MyDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by riskactive on 19.03.2018.
 */

public abstract class AbstractAdapter<ITEM extends Object> extends BaseAdapter {
    private List<ITEM> list = new ArrayList<>();

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public ITEM getItem(int i) {
        return list.get(i);
    }

    public void refresh() {
        list = getList();
        notifyDataSetChanged();
    }


    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = onInflateView(viewGroup);
        }
        ViewHolder vh = new ViewHolder(view, i);
        view.setTag(vh);
        return view;
    }

    public abstract CompoundButton.OnCheckedChangeListener getListener(MyDevice myDevice);

    public abstract List<ITEM> getList();

    public abstract View onInflateView(ViewGroup root);

    public abstract void onCreateViewHolder(View v, ITEM item, int position);

    private class ViewHolder {
        public ViewHolder(View v, int position) {
            ITEM item = getItem(position);
            onCreateViewHolder(v, item, position);
        }
    }

}
