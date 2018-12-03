package com.zeusees.nsfw.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.util.Log;


import com.zeusees.nsfw.Masher;
import com.zeusees.nsfw.bean.DetectResult;
import com.zeusees.nsfw.util.FileUtil;


import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


public class OCRPresenter extends BasePresenter<OCRApi> {

    private Context mContext;
    private OCRApi mView;


    public OCRPresenter(OCRApi mvpView) {
        super(mvpView);
        mView = mvpView;
    }

    public OCRPresenter(Context context, OCRApi mvpView) {
        super(context, mvpView);
        mContext = context;
        mView = mvpView;
    }


    public void recognition(final String path) {
        final Observable observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                e.onNext(path);
            }
        });

        observable.subscribeOn(Schedulers.newThread())
                .map(new Function<String, String>() {
                    @Override
                    public String apply(String path) throws Exception {
                        vedioDetech(path);
                        return "";
                    }
                })

                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<DetectResult>() {
                    @Override
                    public void accept(DetectResult ret) throws Exception {
                        mView.success(0, ret);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        //错误的结果
                    }
                });

    }

    private void vedioDetech(String vedioPath) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(vedioPath);

        long time = FileUtil.getVedioDuration(vedioPath);


        String path = Environment.getExternalStorageDirectory().getPath() + File.separator + "temp.png";


        Log.e("TAG","time------------"+time);
        long size = time / 1000;
        for(long i = 0;i < size;i=i+3) {
            Bitmap bitmap = mmr.getFrameAtTime((long) (i * 1000), MediaMetadataRetriever
                    .OPTION_PREVIOUS_SYNC);
            path = FileUtil.bitMapToFile(path, bitmap);

            DetectResult result = Masher.Detect(path);

            mView.recognition(result, bitmap);

            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }

            Log.e("TAG", "result--------------" + result.toString());
        }
    }
}
