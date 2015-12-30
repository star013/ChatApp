package com.example.admin.mychat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

/**
 * 用于实现Bitmap和byte[]间的相互转换
 * Created by admin on 2015/12/30.
 */
public class BytesBitmap {
    public static Bitmap getBitmap(byte[] data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream baops = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, baops);
        return baops.toByteArray();
    }
}
