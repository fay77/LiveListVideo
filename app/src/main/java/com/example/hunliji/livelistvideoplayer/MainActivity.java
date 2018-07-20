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
import listvideo.ListVideoPlayer;
import listvideo.ListVideoPlayerManager;
import utils.CustomLinearLayoutManager;
import utils.ListVideoVisibleTracker;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.rv)
    RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        final CustomLinearLayoutManager linearLayoutManager = new CustomLinearLayoutManager(this,
                LinearLayoutManager.VERTICAL,
                false);
        recyclerView.setLayoutManager(linearLayoutManager);
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            if (i == 1) {
                list.add("http://jzvd.nathen" + "" +
                        ".cn/c6e3dc12a1154626b3476d9bf3bd7266/6b56c5f0dc31428083757a45764763b0" +
                        "-5287d2089db37e62345123a1be272f8b.mp4");
            } else {
                list.add(
                        "http://pili-vod.hunliji.com/live256preview1531735320-1531735330.mp4");
            }


        }
        LiveListAdapter liveListAdapter = new LiveListAdapter(list, this);
        recyclerView.setAdapter(liveListAdapter);
        liveListAdapter.setOnModeChange(new LiveListAdapter.OnModeChange() {
            @Override
            public void modeChange(int mode) {
                if (mode == ListVideoPlayer.Mode.FULL_SCREEN) {
                    linearLayoutManager.setScrollEnabled(false);
                } else {
                    linearLayoutManager.setScrollEnabled(true);

                }
            }
        });
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

    @Override
    public void onBackPressed() {
        if (ListVideoPlayerManager.getCurrentVideo() != null) {
            if (ListVideoPlayerManager.getCurrentVideo()
                    .getCurrentMode() == ListVideoPlayer.Mode.FULL_SCREEN) {
                ListVideoPlayerManager.getCurrentVideo()
                        .exitFullScreen();
                return;
            }
        }
        super.onBackPressed();
    }
}
