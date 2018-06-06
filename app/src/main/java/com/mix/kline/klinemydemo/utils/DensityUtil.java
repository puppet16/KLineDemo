package com.mix.kline.klinemydemo.utils;

import com.mix.kline.klinemydemo.BaseApplication;

/**
 * Desc:
 * Author ltt
 * Email: litt@mixotc.com
 * Date:  2018/1/31.
 */

public class DensityUtil {
    //dp转px
    public static int dp2px(float dpValue) {
        final float scale = BaseApplication.getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dp(float pxValue) {
        final float scale = BaseApplication.getContext().getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int px2sp(float pxValue) {
        final float scale = BaseApplication.getContext().getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int sp2px(float spValue) {
        final float scale = BaseApplication.getContext().getResources().getDisplayMetrics().density;
        return (int) (spValue * scale + 0.5f);
    }
}
