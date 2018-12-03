package com.zeusees.nsfw;

import android.graphics.Bitmap;

import com.zeusees.nsfw.bean.DetectResult;


/**
 * Created by Yang.kz
 */

public class Masher {
    static {
        System.loadLibrary("nsfw");
    }

    public static native boolean Init(String finemapping_prototxt, String finemapping_caffemodel, String labelPath);

    public static native DetectResult Detect(String path);

    public static native DetectResult vedioDetect(Bitmap bitmap);

}
