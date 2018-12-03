package com.zeusees.nsfw.presenter;

import android.content.Context;


public class BasePresenter<V> {
    public V mvpView;
    public Context mContext;


    public BasePresenter(V mvpView) {

    }

    public BasePresenter(Context context, V mvpView) {
        mContext = context;
    }

}
