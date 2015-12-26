package com.example.admin.mychat;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by admin on 2015/12/26.
 * 记录好友列表每一项，id，姓名，个性签名
 * 也可以是聊天列表，把个性签名项用最后一个聊天对话代替
 */
public class AddrInfo implements Serializable{
    private String id;
    private String sign;
    private String name;
    //public Bitmap avatar;
    AddrInfo(String id,String name,String sign){
        this.id = id;
        this.sign = sign;
        this.name = name;
    }

    String getId(){
        return id;
    }
    String getName(){
        return name;
    }
    String getSign(){
        return sign;
    }

    void setSign(String sign){
        this.sign = sign;
    }
    void setName(String name){
        this.name = name;
    }
}
