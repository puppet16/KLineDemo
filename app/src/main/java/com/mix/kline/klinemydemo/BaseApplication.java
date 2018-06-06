package com.mix.kline.klinemydemo;

import android.app.Application;
import android.content.Context;

import java.lang.ref.WeakReference;

/**
 * Desc:
 * Author ltt
 * Email: litt@mixotc.com
 * Date:  2018/6/5.
 */
public class BaseApplication extends Application {

    private static WeakReference<Context> mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = new WeakReference<>(getApplicationContext());
    }

    public static Context getContext() {
        return mContext.get();
    }

}
