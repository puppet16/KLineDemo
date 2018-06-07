package com.mix.kline.klinemydemo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.mix.kline.klinemydemo.utils.DensityUtil;
import com.wordplat.ikvstockchart.InteractiveKLineView;
import com.wordplat.ikvstockchart.KLineHandler;
import com.wordplat.ikvstockchart.compat.PerformenceAnalyser;
import com.wordplat.ikvstockchart.drawing.HighlightDrawing;
import com.wordplat.ikvstockchart.drawing.KLineVolumeDrawing;
import com.wordplat.ikvstockchart.drawing.StockIndexYLabelDrawing;
import com.wordplat.ikvstockchart.entry.Entry;
import com.wordplat.ikvstockchart.entry.EntrySet;
import com.wordplat.ikvstockchart.entry.StockDataTest;
import com.wordplat.ikvstockchart.entry.StockKLineVolumeIndex;
import com.wordplat.ikvstockchart.marker.XAxisTextMarkerView;
import com.wordplat.ikvstockchart.marker.YAxisTextMarkerView;
import com.wordplat.ikvstockchart.render.KLineRender;

import java.io.InputStream;

public class ShowTogetherActivity extends AppCompatActivity {
    public static final String TAG = "ShowTogetherActivity";
    InteractiveKLineView mKLineView;
    TextView mTvTest;
    FrameLayout mFlView;
    private KLineRender mKLineRender;
    View mView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mView = LayoutInflater.from(this).inflate(R.layout.activity_together, null);
        mFlView = mView.findViewById(R.id.fl_kline);
        setContentView(mView);
        mKLineView = mView.findViewById(R.id.kLine);
        mTvTest = mView.findViewById(R.id.tv_test);
        initUI();
        loadKLineData();
        Log.d(TAG, "onCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    private void initUI() {
        mKLineView.setEnableLeftRefresh(false);
        mKLineView.setEnableLeftRefresh(false);
        mKLineRender = (KLineRender) mKLineView.getRender();

        final int paddingTop = DensityUtil.dp2px(10);
        final int stockMarkerViewHeight = DensityUtil.dp2px(15);

//        // MACD
//        HighlightDrawing macdHighlightDrawing = new HighlightDrawing();
//        macdHighlightDrawing.addMarkerView(new YAxisTextMarkerView(stockMarkerViewHeight));
//
//        StockMACDIndex macdIndex = new StockMACDIndex();
//        macdIndex.addDrawing(new MACDDrawing());
//        macdIndex.addDrawing(new StockIndexYLabelDrawing());
//        macdIndex.addDrawing(macdHighlightDrawing);
//        macdIndex.setPaddingTop(paddingTop);
//        mKLineRender.addStockIndex(macdIndex);

        //Volume
        HighlightDrawing volumeHighlightDrawing = new HighlightDrawing();
        volumeHighlightDrawing.addMarkerView(new YAxisTextMarkerView(stockMarkerViewHeight));

        StockKLineVolumeIndex volumeIndex = new StockKLineVolumeIndex();
        volumeIndex.addDrawing(new KLineVolumeDrawing());
        volumeIndex.addDrawing(new StockIndexYLabelDrawing());
        volumeIndex.addDrawing(volumeHighlightDrawing);
        volumeIndex.setPaddingTop(paddingTop);
        mKLineRender.addStockIndex(volumeIndex);
        mKLineRender.addMarkerView(new YAxisTextMarkerView(stockMarkerViewHeight));
        mKLineRender.addMarkerView(new XAxisTextMarkerView(stockMarkerViewHeight));
        mKLineView.setKLineHandler(new KLineHandler() {
            @Override
            public void onLeftRefresh() {

            }

            @Override
            public void onRightRefresh() {

            }

            @Override
            public void onSingleTap(MotionEvent e, float x, float y) {

            }

            @Override
            public void onDoubleTap(MotionEvent e, float x, float y) {
                if (mKLineRender.getKLineRect().contains(x, y)) {
                    mKLineRender.zoomIn(x, y);
                }
            }

            @Override
            public void onHighlight(Entry entry, int entryIndex, float x, float y) {

            }

            @Override
            public void onCancelHighlight() {

            }
        });
    }

    private void loadKLineData() {
        new AsyncTask<Void, Void, Void>() {

            private EntrySet entrySet;

            @Override
            protected Void doInBackground(Void... params) {

                PerformenceAnalyser.getInstance().addWatcher();

                String kLineData = "";
                try {
                    InputStream in = getResources().getAssets().open("kline1.txt");
                    int length = in.available();
                    byte[] buffer = new byte[length];
                    in.read(buffer);
                    kLineData = new String(buffer, "UTF-8");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                PerformenceAnalyser.getInstance().addWatcher();

                entrySet = StockDataTest.parseKLineData(kLineData);

                PerformenceAnalyser.getInstance().addWatcher();

                entrySet.computeStockIndex();

                PerformenceAnalyser.getInstance().addWatcher();

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mKLineView.setEntrySet(entrySet);

                PerformenceAnalyser.getInstance().addWatcher();

                mKLineView.notifyDataSetChanged();

                PerformenceAnalyser.getInstance().addWatcher();
            }
        }.execute();
    }

    public static Intent createIntent(Context context) {
        Intent intent = new Intent(context, ShowTogetherActivity.class);
        return intent;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mFlView.removeView(mKLineView);
            setContentView(mKLineView);
            Log.d(TAG, "切成横屏");
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.d(TAG, "切成竖屏");
            ((ViewGroup) mKLineView.getParent()).removeView(mKLineView);
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mKLineView.getLayoutParams();
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            mKLineView.setLayoutParams(layoutParams);
            mFlView.addView(mKLineView);
            setContentView(mView);
        }
    }

    public void onClick(View v) {
        boolean isVertical = (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
        if (isVertical) {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
    }

    @Override
    public void onBackPressed() {
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        } else {
            super.onBackPressed();
        }
    }
}
