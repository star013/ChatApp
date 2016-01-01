package com.example.admin.mychat;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * 记录好友列表每一项，id，姓名，个性签名，以及头像位置
 * 序列化，便于头像文件的传输
 * Created by admin on 2015/12/26.
 */
public class AddrInfo implements Serializable{
    private String id;
    private String sign;
    private String name;
    private BitMapData avatar;
    private String avatar_path = "";
    AddrInfo(String id,String name,String sign,Bitmap avatar,String avatar_path){
        this.id = id;
        this.sign = sign;
        this.name = name;
        this.avatar = new BitMapData(BytesBitmap.getBytes(avatar));
        this.avatar_path = avatar_path;
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
    public String getAvatar_path(){
        return avatar_path;
    }

    public void setAvatar(BitMapData avatar){
        this.avatar = avatar;
    }
    public void setSign(String sign){
        this.sign = sign;
    }
    public void setName(String name){
        this.name = name;
    }
    public void setAvatar_path(String path){
        avatar_path = path;
    }

}
