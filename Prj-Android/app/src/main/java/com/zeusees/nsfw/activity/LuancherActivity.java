package com.zeusees.nsfw.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.zeusees.nsfw.R;

import org.opencv.android.OpenCVLoader;

public class LuancherActivity extends BaseActivity {


    static {

        if (OpenCVLoader.initDebug()) {
            Log.d("Opencv", "opencv load_success");

        } else {
            Log.d("Opencv", "opencv can't load opencv .");

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        luancherHome();
    }


    private void luancherHome() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(LuancherActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 1000);
    }
}
