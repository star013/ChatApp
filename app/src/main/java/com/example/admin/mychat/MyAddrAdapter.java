package com.example.admin.mychat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by admin on 2015/12/26.
 * 为了配合 ListView 的使用和自定义的 ArrayList 能够衔接
 */
public class MyAddrAdapter extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private List<AddrInfo> addrInfoList = null;

    public MyAddrAdapter(Context context){
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return addrInfoList.size();
    }

    @Override
    public Object getItem(int position) {
        return addrInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * 自定义的匹配数据和 ListView
     * */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = layoutInflater.inflate(R.layout.list_item_friend_addr,null);
        TextView title = (TextView) convertView.findViewById(R.id.itemTitle);
        TextView text = (TextView) convertView.findViewById(R.id.itemText);
        AddrInfo a = addrInfoList.get(position);
        title.setText(a.getName());
        text.setText(a.getSign());
        return convertView;
    }
    /**
     * 加载数据
     * */
    public void loadData(List<AddrInfo> addrInfoList){
        this.addrInfoList = addrInfoList;
    }

    /**
     * 添加一个项目
     */
    public void add(AddrInfo a){
        addrInfoList.add(a);
        notifyDataSetChanged();
    }
    /**
     * 删除数据中的一项
     * */
    public void remove(int position){
        addrInfoList.remove(position);
        // 更新 ListView
        notifyDataSetChanged();
    }
}
