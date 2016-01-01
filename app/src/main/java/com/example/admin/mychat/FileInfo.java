package com.example.admin.mychat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * 用来实现文件的传输，可以序列化，用传输对象的流来传输
 * Created by admin on 2016/1/1.
 */
public class FileInfo implements Serializable{
    private String file_path;
    private String file_name;
    private byte[] file_bytes = null;
    public FileInfo(String file_path,String file_name){
        this.file_path = file_path;
        this.file_name = file_name;
    }

    public String getFile_path(){
        return file_path;
    }
    public String getFile_name(){
        return file_name;
    }

    public void setFile_path(String file_path){
        this.file_path = file_path;
    }
    public void setFile_name(String file_name){
        this.file_name = file_name;
    }
    public void storeFileToBytes(){
        ByteArrayOutputStream byteout = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int readnum = 0;
        try {
            FileInputStream fin = new FileInputStream(file_path);
            while((readnum = fin.read(buf)) != -1){
                byteout.write(buf,0,readnum);
            }
            file_bytes = byteout.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void storeBytesToFile(){
        try {
            File file = new File(file_path);
            if (file.exists())
                file.delete();
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(file_bytes);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
