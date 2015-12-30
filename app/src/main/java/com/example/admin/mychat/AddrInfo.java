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
    private BitMapData avatar;
    AddrInfo(String id,String name,String sign,Bitmap avatar){
        this.id = id;
        this.sign = sign;
        this.name = name;
        this.avatar = new BitMapData(BytesBitmap.getBytes(avatar));
    }

    public String getId(){
        return id;
    }
    public String getName(){
        return name;
    }
    public String getSign(){
        return sign;
    }
    public Bitmap getAvatar(){
        return BytesBitmap.getBitmap(avatar.getBitmapBytes());
    }
}
