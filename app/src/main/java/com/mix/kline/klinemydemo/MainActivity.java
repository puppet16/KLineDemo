package com.mix.kline.klinemydemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.btn_left_right_refresh:
                intent = LeftAndRightRefreshActivity.createIntent(this);
                break;
            case R.id.btn_no_left_right_refresh:
                intent = DisableRefreshActivity.createIntent(this);
                break;
            case R.id.btn_together:
                intent = ShowTogetherActivity.createIntent(this);
                break;
            case R.id.btn_fen:
                intent = TimeLineActivity.createIntent(this);
                break;
        }
        startActivity(intent);
    }
}
