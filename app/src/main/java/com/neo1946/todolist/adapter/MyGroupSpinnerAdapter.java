package com.neo1946.todolist.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.neo1946.todolist.R;
import com.neo1946.todolist.bean.Group;
import com.neo1946.todolist.util.ColorUtil;

import java.util.List;


/**
 * @author ouyangzhaoxian on 2019/03/12
 * 紧急度的Adapter
 */
public class MyGroupSpinnerAdapter extends BaseAdapter {
    private List<Group> mList;
    private Context mContext;

    public MyGroupSpinnerAdapter(Context pContext, List<Group> pList) {
        this.mContext = pContext;
        this.mList = pList;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater _LayoutInflater=LayoutInflater.from(mContext);
        convertView=_LayoutInflater.inflate(R.layout.spinner_item, null);
        if(convertView!=null) {
            View colorView = (View)convertView.findViewById(R.id.v_color);
            ColorUtil.setViewColor(colorView,mList.get(position).getId());
            TextView _TextView1=(TextView)convertView.findViewById(R.id.tv_title);
            //紧急度id只用于内部 用户看颜色即可
//            _TextView1.setText(mList.get(position).getName());
        }
        return convertView;
    }
}
