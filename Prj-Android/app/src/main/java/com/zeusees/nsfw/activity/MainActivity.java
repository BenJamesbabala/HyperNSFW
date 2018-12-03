package com.zeusees.nsfw.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.zeusees.nsfw.R;
import com.zeusees.nsfw.bean.DetectResult;
import com.zeusees.nsfw.presenter.OCRApi;
import com.zeusees.nsfw.presenter.OCRPresenter;
import com.zeusees.nsfw.util.FileUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.zeusees.nsfw.Masher;


public class MainActivity extends BaseActivity implements OCRApi {
    private static final int REQUEST_CODE_IMAGE_OP = 2;
    private static final int REQUEST_CODE_VEDIO_OP = 3;


    @BindView(R.id.show_image)
    ImageView show_image;

    @BindView(R.id.result_text)
    TextView result_text;

    private OCRPresenter ocrPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        requestCameraPermission();

        ocrPresenter = new OCRPresenter(this,this);
    }

    @OnClick({R.id.use_photo, R.id.use_vedio})
    public void Onclick(View v) {
        switch (v.getId()) {
            case R.id.use_photo:
                Intent in3 = new Intent(
                        Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(in3, REQUEST_CODE_IMAGE_OP);
                break;

            case R.id.use_vedio:
                Intent in4 = new Intent(
                        Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                in4.setType("video/*");
                startActivityForResult(in4, REQUEST_CODE_VEDIO_OP);
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_IMAGE_OP && resultCode == RESULT_OK) {
            String path = FileUtil.getPathFromURL(this, data);

            Uri image_uri = data.getData();
            Glide.with(MainActivity.this).load(image_uri).into(show_image);

            long start = System.currentTimeMillis();
            DetectResult result = Masher.Detect(path);
            long end = System.currentTimeMillis();

            long time = end - start;

            String show_text = "类别 ：" + result.getClassId() + "\n信心：" + result.getConfidence() + "\ntime：" + time + "ms";
            result_text.setText(show_text);

            Log.e("TAG", "result---------------" + result.toString());
        }

        if (requestCode == REQUEST_CODE_VEDIO_OP && resultCode == RESULT_OK) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                String path = FileUtil.get_vedio_path_from_URI(this, uri);
                ocrPresenter.recognition(path);

            }
        }
    }


    //API >=23
    @TargetApi(Build.VERSION_CODES.M)
    public void requestCameraPermission() {
        final int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int location = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int location2 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int readStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int writeStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission == PackageManager.PERMISSION_GRANTED &&
                location == PackageManager.PERMISSION_GRANTED && location2 == PackageManager.PERMISSION_GRANTED
                && readStorage == PackageManager.PERMISSION_GRANTED && writeStorage == PackageManager.PERMISSION_GRANTED
                ) {
            //TODO
        } else {//请求权限
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, 1);
        }
    }

    @Override
    public void success(int num, final DetectResult result) {

    }

    @Override
    public void recognition(final DetectResult result, final Bitmap bitmap) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                show_image.setImageBitmap(bitmap);

                String show_text = "类别 ：" + result.getClassId() + "\n信心：" + result.getConfidence();
                result_text.setText(show_text);
            }
        });

    }
}
