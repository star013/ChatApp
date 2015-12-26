package com.example.admin.mychat;

import java.io.Serializable;

/**
 * Created by admin on 2015/12/26.
 * 聊天记录的每一句话的记录格式
 */
public class ChatInfo implements Serializable{
    private String date;
    private String text;
    // 是否为对方的信息
    private boolean isCome;

    public ChatInfo(String date,String text,boolean isCome){
        this.date = date;
        this.text = text;
        this.isCome = isCome;
    }

    public String getDate(){
        return date;
    }

    public String getText(){
        return text;
    }

    public boolean getIsCome(){
        return isCome;
    }

    public void setDate(String date){
        this.date = date;
    }

    public void setText(String text){
        this.text = text;
    }

    public void setIsCome(boolean isCome){
        this.isCome = isCome;
    }
}
