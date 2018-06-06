package com.mix.kline.klinemydemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.wordplat.ikvstockchart.InteractiveKLineView;
import com.wordplat.ikvstockchart.entry.Entry;
import com.wordplat.ikvstockchart.entry.EntrySet;
import com.wordplat.ikvstockchart.render.TimeLineRender;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TimeLineActivity extends AppCompatActivity {

    @BindView(R.id.timeLineView)
    InteractiveKLineView timeLineView;
    private final EntrySet entrySet = new EntrySet();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_timeline);
        ButterKnife.bind(this);
        initUI();
        loadData();
    }

    private void loadData() {
        for (int i = 0; i < 200; i++) {
            Entry entry = new Entry(i + 1 * 100, (i + 1) * 1000, "");
            entrySet.addEntry(entry);
        }
        entrySet.getEntryList().get(0).setXLabel("09:30");
        entrySet.getEntryList().get(2).setXLabel("11:30/13:00");
        entrySet.getEntryList().get(4).setXLabel("15:00");

        timeLineView.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void initUI() {
        timeLineView.setEntrySet(entrySet);
        timeLineView.setRender(new TimeLineRender());
    }

    public static Intent createIntent(Context context) {
        Intent intent = new Intent(context, TimeLineActivity.class);
        return intent;
    }
}
