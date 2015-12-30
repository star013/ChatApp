package com.example.admin.mychat;

import android.graphics.drawable.BitmapDrawable;

import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * 用来对 BitMap 数据进行序列化，以便传输
 * Created by admin on 2015/12/30.
 */
public class BitMapData implements Serializable {
    /**
     * serialVersionUID解释:
     * http://www.blogjava.net/invisibletank/archive/2007/11/15/160684.html
     */
    private static final long serialVersionUID = 1L;
    private byte[] bitmapBytes = null;
    public BitMapData(byte[] bitmapBytes){
        this.bitmapBytes = bitmapBytes;
    }
    public byte[] getBitmapBytes() {
        return this.bitmapBytes;
    }
}
