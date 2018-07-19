package com.example.hunliji.livelistvideoplayer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import adapter.LiveListAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import utils.ListVideoVisibleTracker;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.rv)
    RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL,
                false);
        recyclerView.setLayoutManager(linearLayoutManager);
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            list.add(
                    "http://pili-vod.hunliji.com/live256preview1531735320-1531735330.mp4");
        }
        LiveListAdapter liveListAdapter = new LiveListAdapter(list, this);
        recyclerView.setAdapter(liveListAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ListVideoVisibleTracker.addScrollView(recyclerView);
        ListVideoVisibleTracker.setScreenView(recyclerView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ListVideoVisibleTracker.removeScrollView(recyclerView);
        ListVideoVisibleTracker.removeScreenView(recyclerView);

    }
}
