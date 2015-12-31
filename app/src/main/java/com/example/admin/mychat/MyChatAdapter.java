package com.example.admin.mychat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
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
    private String friend_avatar_path;
    private String my_avatar_path;

    public MyChatAdapter(Context context,List<ChatInfo> chatInfoList,String friend_avatar_path,String my_avatar_path){
        this.layoutInflater = LayoutInflater.from(context);
        this.chatInfoList = chatInfoList;
        this.friend_avatar_path = friend_avatar_path;
        this.my_avatar_path = my_avatar_path;
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

    /**
     * 更新 ListView 中的每一项
     * @param convertView 表示上一次使用过的View，用setTag()保存，用getTag()提取上一次项的信息
     */
    @Override
    synchronized public View getView(int position, View convertView, ViewGroup parent) {
        ChatInfo a = chatInfoList.get(position);
        ViewHolder viewHolder = null;
        Bitmap bitmap = null;
        if (convertView==null){
            viewHolder = new ViewHolder();
            if (a.getIsCome()){
                // 消息是对方发过来的
                convertView = layoutInflater.inflate(R.layout.chat_text_left,null);
                bitmap = BitmapFactory.decodeFile(friend_avatar_path);
                viewHolder.avatar = (ImageView) convertView.findViewById(R.id.friend_avatar);
            }else{
                convertView = layoutInflater.inflate(R.layout.chat_text_right,null);
                bitmap = BitmapFactory.decodeFile(my_avatar_path);
                viewHolder.avatar = (ImageView) convertView.findViewById(R.id.my_avatar);
            }
            viewHolder.avatar.setImageBitmap(bitmap);
            viewHolder.time = (TextView) convertView.findViewById(R.id.tv_sendtime);
            viewHolder.content = (TextView) convertView.findViewById(R.id.tv_chatcontent);
            viewHolder.isCome = a.getIsCome();
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder)convertView.getTag();
            // 如果旧的信息和新的信息不同
            if (viewHolder.isCome != a.getIsCome()){
                viewHolder = new ViewHolder();
                if (a.getIsCome()){
                    // 消息是对方发过来的
                    convertView = layoutInflater.inflate(R.layout.chat_text_left, null);
                    bitmap = BitmapFactory.decodeFile(friend_avatar_path);
                    viewHolder.avatar = (ImageView) convertView.findViewById(R.id.friend_avatar);
                }else{
                    convertView = layoutInflater.inflate(R.layout.chat_text_right, null);
                    bitmap = BitmapFactory.decodeFile(my_avatar_path);
                    viewHolder.avatar = (ImageView) convertView.findViewById(R.id.my_avatar);
                }
                viewHolder.avatar.setImageBitmap(bitmap);
                viewHolder.time = (TextView) convertView.findViewById(R.id.tv_sendtime);
                viewHolder.content = (TextView) convertView.findViewById(R.id.tv_chatcontent);
                viewHolder.isCome = a.getIsCome();
                convertView.setTag(viewHolder);
            }
        }
        viewHolder.time.setText(a.getDate());
        viewHolder.content.setText(a.getText());


        return convertView;
    }

    /**
     * 通过ViewHolder显示项的内容
     * */
    static class ViewHolder{
        public TextView time;
        public TextView content;
        public boolean isCome;
        public ImageView avatar;
    }

}
