package com.example.admin.mychat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2015/12/26.
 * 为了配合 ListView 的使用和自定义的 ArrayList 能够衔接
 */
public class MyChatAdapter extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private List<ChatInfo> chatInfoList = null;

    public MyChatAdapter(Context context){
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return chatInfoList.size();
    }

    @Override
    public Object getItem(int position) {
        return chatInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatInfo a = chatInfoList.get(position);
        if (a.getIsCome()){
            // 消息是对方发过来的
            convertView = layoutInflater.inflate(R.layout.chat_text_left,null);
        }else{
            convertView = layoutInflater.inflate(R.layout.chat_text_right,null);
        }
        TextView time = (TextView) convertView.findViewById(R.id.tv_sendtime);
        TextView content = (TextView) convertView.findViewById(R.id.tv_chatcontent);

        time.setText(a.getDate());
        content.setText(a.getText());

        return convertView;
    }

    /**
     * 加载数据
     * */
    public void loadData(List<ChatInfo> chatInfoList){
        this.chatInfoList = chatInfoList;
    }
    /**
     * 增加一条记录的情况
     * */
    public void addOneData(ChatInfo a){
        chatInfoList.add(a);
    }
}
