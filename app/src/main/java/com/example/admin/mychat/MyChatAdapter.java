package com.example.admin.mychat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2015/12/26.
 * 为了配合 ListView 的使用和自定义的 ArrayList 能够衔接
 */
public class MyChatAdapter extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private List<ChatInfo> chatInfoList;

    public MyChatAdapter(Context context,List<ChatInfo> chatInfoList){
        this.layoutInflater = LayoutInflater.from(context);
        this.chatInfoList = chatInfoList;
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
        ViewHolder viewHolder = null;
        if (convertView==null){
            if (a.getIsCome()){
                // 消息是对方发过来的
                convertView = layoutInflater.inflate(R.layout.chat_text_left,null);
            }else{
                convertView = layoutInflater.inflate(R.layout.chat_text_right,null);
            }
            viewHolder = new ViewHolder();
            viewHolder.time = (TextView) convertView.findViewById(R.id.tv_sendtime);
            viewHolder.content = (TextView) convertView.findViewById(R.id.tv_chatcontent);
            viewHolder.isCome = a.getIsCome();
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder)convertView.getTag();
        }
        viewHolder.time.setText(a.getDate());
        viewHolder.content.setText(a.getText());

        return convertView;
    }

    /**
     * 更新数据
     * */
    public void refresh(List<ChatInfo> newList){
        chatInfoList = newList;
        notifyDataSetChanged();
    }

    /**
     * 通过ViewHolder显示项的内容
     * */
    static class ViewHolder{
        public TextView time;
        public TextView content;
        public boolean isCome;
    }
}
