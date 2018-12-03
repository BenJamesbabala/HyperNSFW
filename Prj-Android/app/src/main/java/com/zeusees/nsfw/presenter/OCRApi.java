package com.zeusees.nsfw.presenter;


import android.graphics.Bitmap;

import com.zeusees.nsfw.bean.DetectResult;

public interface OCRApi {
    void success(int num, DetectResult result);
    void recognition(DetectResult result, Bitmap bitmap);
}


