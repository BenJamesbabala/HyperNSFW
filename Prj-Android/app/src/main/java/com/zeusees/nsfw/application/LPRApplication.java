package com.zeusees.nsfw.application;

import android.app.Application;
import android.os.Environment;

import com.zeusees.nsfw.util.FileUtil;

import java.io.File;

import com.zeusees.nsfw.Masher;

public class LPRApplication extends Application {

    private long handle;

    @Override
    public void onCreate() {
        super.onCreate();
        initRecognizer();
    }


    public void initRecognizer() {
        String assetPath = "NSFW";
        String sdcardPath = Environment.getExternalStorageDirectory()
                + File.separator + "NSFW";
        FileUtil.copyFilesFromAssets(this, assetPath, sdcardPath);

        String finemapping_prototxt = sdcardPath
                + File.separator + "mobilenet_v2_deploy.prototxt";
        String finemapping_caffemodel = sdcardPath
                + File.separator + "HyperNSFW.caffemodel";

        String labelpath = sdcardPath
                + File.separator + "label.txt";

        Masher.Init(
                finemapping_prototxt, finemapping_caffemodel, labelpath
        );
    }
}
