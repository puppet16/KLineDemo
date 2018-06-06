package com.mix.kline.klinemydemo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.mix.kline.klinemydemo.utils.DensityUtil;
import com.wordplat.ikvstockchart.InteractiveKLineView;
import com.wordplat.ikvstockchart.compat.PerformenceAnalyser;
import com.wordplat.ikvstockchart.drawing.HighlightDrawing;
import com.wordplat.ikvstockchart.drawing.KLineVolumeDrawing;
import com.wordplat.ikvstockchart.drawing.MACDDrawing;
import com.wordplat.ikvstockchart.drawing.StockIndexYLabelDrawing;
import com.wordplat.ikvstockchart.entry.EntrySet;
import com.wordplat.ikvstockchart.entry.StockDataTest;
import com.wordplat.ikvstockchart.entry.StockKLineVolumeIndex;
import com.wordplat.ikvstockchart.entry.StockMACDIndex;
import com.wordplat.ikvstockchart.marker.XAxisTextMarkerView;
import com.wordplat.ikvstockchart.marker.YAxisTextMarkerView;
import com.wordplat.ikvstockchart.render.KLineRender;

import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ShowTogetherActivity extends AppCompatActivity {

    @BindView(R.id.kLineView)
    InteractiveKLineView kLineView;
    private KLineRender kLineRender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_together);
        ButterKnife.bind(this);
        initUI();
        loadKLineData();
    }

    private void initUI() {
        kLineView.setEnableLeftRefresh(false);
        kLineView.setEnableLeftRefresh(false);
        kLineRender = (KLineRender) kLineView.getRender();

        final int paddingTop = DensityUtil.dp2px(10);
        final int stockMarkerViewHeight = DensityUtil.dp2px(15);

        // MACD
        HighlightDrawing macdHighlightDrawing = new HighlightDrawing();
        macdHighlightDrawing.addMarkerView(new YAxisTextMarkerView(stockMarkerViewHeight));

        StockMACDIndex macdIndex = new StockMACDIndex();
        macdIndex.addDrawing(new MACDDrawing());
        macdIndex.addDrawing(new StockIndexYLabelDrawing());
        macdIndex.addDrawing(macdHighlightDrawing);
        macdIndex.setPaddingTop(paddingTop);
        kLineRender.addStockIndex(macdIndex);

        //Volume
        HighlightDrawing volumeHighlightDrawing = new HighlightDrawing();
        volumeHighlightDrawing.addMarkerView(new YAxisTextMarkerView(stockMarkerViewHeight));

        StockKLineVolumeIndex volumeIndex = new StockKLineVolumeIndex();
        volumeIndex.addDrawing(new KLineVolumeDrawing());
        volumeIndex.addDrawing(new StockIndexYLabelDrawing());
        volumeIndex.addDrawing(volumeHighlightDrawing);
        volumeIndex.setPaddingTop(paddingTop);
        kLineRender.addStockIndex(volumeIndex);
//        // RSI
//        HighlightDrawing rsiHighlightDrawing = new HighlightDrawing();
//        rsiHighlightDrawing.addMarkerView(new YAxisTextMarkerView(stockMarkerViewHeight));
//
//        StockRSIIndex rsiIndex = new StockRSIIndex();
//        rsiIndex.addDrawing(new RSIDrawing());
//        rsiIndex.addDrawing(new StockIndexYLabelDrawing());
//        rsiIndex.addDrawing(rsiHighlightDrawing);
//        rsiIndex.setPaddingTop(paddingTop);
//        kLineRender.addStockIndex(rsiIndex);
//
//        // KDJ
//        HighlightDrawing kdjHighlightDrawing = new HighlightDrawing();
//        kdjHighlightDrawing.addMarkerView(new YAxisTextMarkerView(stockMarkerViewHeight));
//
//        StockKDJIndex kdjIndex = new StockKDJIndex();
//        kdjIndex.addDrawing(new KDJDrawing());
//        kdjIndex.addDrawing(new StockIndexYLabelDrawing());
//        kdjIndex.addDrawing(kdjHighlightDrawing);
//        kdjIndex.setPaddingTop(paddingTop);
//        kLineRender.addStockIndex(kdjIndex);

        kLineRender.addMarkerView(new YAxisTextMarkerView(stockMarkerViewHeight));
        kLineRender.addMarkerView(new XAxisTextMarkerView(stockMarkerViewHeight));
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
                kLineView.setEntrySet(entrySet);

                PerformenceAnalyser.getInstance().addWatcher();

                kLineView.notifyDataSetChanged();

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

        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

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
}
