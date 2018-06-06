package com.mix.kline.klinemydemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


public class DisableRefreshActivity extends LeftAndRightRefreshActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        kLineLayout.getKLineView().setEnableLeftRefresh(false);
    }

    public static Intent createIntent(Context context) {
        Intent intent = new Intent(context, DisableRefreshActivity.class);
        return intent;
    }
}
