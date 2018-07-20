package com.example.hunliji.livelistvideoplayer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
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

    public static String[] videoUrlList =
            {
                    "http://jzvd.nathen.cn/c494b340ff704015bb6682ffde3cd302/64929c369124497593205a4190d7d128-5287d2089db37e62345123a1be272f8b.mp4",
                    "http://jzvd.nathen.cn/63f3f73712544394be981d9e4f56b612/69c5767bb9e54156b5b60a1b6edeb3b5-5287d2089db37e62345123a1be272f8b.mp4",
                    "http://jzvd.nathen.cn/b201be3093814908bf987320361c5a73/2f6d913ea25941ffa78cc53a59025383-5287d2089db37e62345123a1be272f8b.mp4",
                    "http://jzvd.nathen.cn/d2438fd1c37c4618a704513ad38d68c5/68626a9d53ca421c896ac8010f172b68-5287d2089db37e62345123a1be272f8b.mp4",
                    "http://jzvd.nathen.cn/25a8d119cfa94b49a7a4117257d8ebd7/f733e65a22394abeab963908f3c336db-5287d2089db37e62345123a1be272f8b.mp4",
                    "http://jzvd.nathen.cn/7512edd1ad834d40bb5b978402274b1a/9691c7f2d7b74b5e811965350a0e5772-5287d2089db37e62345123a1be272f8b.mp4",
                    "http://jzvd.nathen.cn/c6e3dc12a1154626b3476d9bf3bd7266/6b56c5f0dc31428083757a45764763b0-5287d2089db37e62345123a1be272f8b.mp4"
            };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        final CustomLinearLayoutManager linearLayoutManager = new CustomLinearLayoutManager(this,
                LinearLayoutManager.VERTICAL,
                false);
        recyclerView.setLayoutManager(linearLayoutManager);
        List<String> list = Arrays.asList(videoUrlList);

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
