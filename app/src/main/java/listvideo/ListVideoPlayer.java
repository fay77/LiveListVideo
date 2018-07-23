package listvideo;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;


import com.example.hunliji.livelistvideoplayer.R;

import java.lang.ref.WeakReference;

import utils.CommonUtil;
import utils.VideoUtil;


public class ListVideoPlayer extends FrameLayout {
    private static final int MSG_UPDATE_TIME_PROGRESS = 100;

    public class STATE {
        public static final int ERROR = -1;
        public static final int NORMAL = 0;
        public static final int PREPARING = 1;
        public static final int PLAYING = 2;
        public static final int PAUSE = 3;
        public static final int COMPLETE = 4;
    }

    public class Mode {
        public static final int FULL_SCREEN = 1001;
        public static final int TINY_SCREEN = 0;

    }


    public enum ScaleType {
        FIT_CENTER,
        CENTER_CROP
    }

    FrameLayout textureContainer;
    ProgressBar loadingBar;
    private UpdateTimeHandler handler;


    private Uri source;
    private int currentState;
    private int currentMode;
    private ViewGroup viewGroup;
    private SeekBar mSeekBar;
    private TextView mPositionTimeTv;
    private TextView mDurationTimeTv;

    private ScaleType scaleType= ScaleType.CENTER_CROP;

    private OnStateChangeListener onStateChangeListener;

    private OnModeChangeListener onModeChangeListener;

    private OnBufferingUpdateListener onBufferingUpdateListener;

    private View mControllerView;

    public ListVideoPlayer(@NonNull Context context) {
        super(context);
        init();
    }

    public ListVideoPlayer(
            @NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ListVideoPlayer(
            @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ListVideoPlayer(
            @NonNull Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        View.inflate(getContext(), R.layout.widget_list_video_player, this);
        textureContainer = findViewById(R.id.texture_container);
        loadingBar = findViewById(R.id.progress_loading);
    }

    public void setScaleType(ScaleType scaleType) {
        this.scaleType = scaleType;
    }

    public void setSource(Uri source) {
        if (isCurrentVideo() && MediaManager.INSTANCE()
                .getSource()
                .equals(source)) {
            return;
        }
        this.source = source;
        currentState = STATE.NORMAL;
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        if (onStateChangeListener != null) {
            onStateChangeListener.OnStateChange(currentState);
        }
    }

    public void enterFullScreen() {




        if (currentMode == Mode.FULL_SCREEN)
            return;
        currentMode = Mode.FULL_SCREEN;
        if (currentState == STATE.PAUSE) {
          onPlaying();
        }
        if (onModeChangeListener != null) {
            onModeChangeListener.OnModeChange(currentMode);
        }
        Log.d("fff", "进入全屏了" + currentMode);

        // 隐藏ActionBar、状态栏，并横屏
//                NiceUtil.hideActionBar(mContext);
        //        NiceUtil.scanForActivity(mContext)
        //                .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        VideoUtil.scanForActivity(getContext())
//                .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        CommonUtil.hideActionBar(getContext());
        ViewGroup contentView = (ViewGroup) VideoUtil.scanForActivity(getContext())
                .findViewById(android.R.id.content);
        if (currentMode == Mode.TINY_SCREEN) {
            contentView.removeView(textureContainer);
        } else {
            this.removeView(textureContainer);
        }
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        viewGroup = (ViewGroup) textureContainer.getParent();
        if (viewGroup != null) {
            viewGroup.removeView(textureContainer);
        }
        contentView.addView(textureContainer, params);
        mControllerView.setRotation(90);
        if (MediaManager.INSTANCE()
                .getTextureView() != null) {
            MediaManager.INSTANCE().getTextureView().setRotation(90);
        }
        ViewGroup.LayoutParams layoutParams = mControllerView.getLayoutParams();
        layoutParams.width = CommonUtil.getDeviceSize(getContext()).y;
        layoutParams.height = CommonUtil.getDeviceSize(getContext()).x;

    }

    public boolean exitFullScreen() {

        if (currentMode == Mode.FULL_SCREEN) {
            currentMode = Mode.TINY_SCREEN;
            if (onModeChangeListener != null) {
                onModeChangeListener.OnModeChange(currentMode);
            }

            //            NiceUtil.showActionBar(mContext);
            //            NiceUtil.scanForActivity(mContext)
            //                    .setRequestedOrientation(ActivityInfo
            // .SCREEN_ORIENTATION_PORTRAIT);
            CommonUtil.showActionBar(getContext());

            VideoUtil.scanForActivity(getContext())
                    .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            ViewGroup contentView = (ViewGroup) VideoUtil.scanForActivity(getContext())
                    .findViewById(android.R.id.content);
            contentView.removeView(textureContainer);
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            viewGroup.addView(textureContainer, params);
            //            this.addView(textureContainer, params);
            mControllerView.setRotation(0);
            if (MediaManager.INSTANCE()
                    .getTextureView() != null) {
                MediaManager.INSTANCE().getTextureView().setRotation(0);
            }

            LayoutParams layoutParams = new LayoutParams(ViewGroup
                    .LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER);
            mControllerView.setLayoutParams(layoutParams);




            return true;
        }
        return false;
    }
    public void startVideo() {
        if (source == null) {
            return;
        }
        ListVideoPlayerManager.setCurrentVideo(this);
        initTextureView(scaleType);
        addTextureView();
        addSeekBar();
        AudioManager mAudioManager = (AudioManager) getContext().getSystemService(Context
                .AUDIO_SERVICE);
        if (mAudioManager != null) {
            mAudioManager.requestAudioFocus(ListVideoPlayerManager.onAudioFocusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        }
        VideoUtil.scanForActivity(getContext())
                .getWindow()
                .addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        MediaManager.INSTANCE()
                .setSource(source);
        currentState = STATE.PREPARING;
        if (onStateChangeListener != null) {
            onStateChangeListener.OnStateChange(currentState);
        }
        loadingBar.setVisibility(VISIBLE);
        if (onStateChangeListener != null) {
            onStateChangeListener.OnStateChange(currentState);
        }
    }

    public void onPrepared() {
//        long position = VideoUtil.getSavedProgress(getContext(), source.toString());
//        if (position > 0) {
//            MediaManager.INSTANCE()
//                    .seekTo((int) position);
//        }
        onPlaying();
    }

    public void onPlaying() {
        currentState = STATE.PLAYING;
        if (handler != null) {
            updatePlayTimeAndProgress();
        }
        MediaManager.INSTANCE()
                .start();
        loadingBar.setVisibility(GONE);
        if (onStateChangeListener != null) {
            onStateChangeListener.OnStateChange(currentState);
        }
    }

    public void onPause() {
        currentState = STATE.PAUSE;
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        MediaManager.INSTANCE()
                .pause();
        loadingBar.setVisibility(GONE);
        if (onStateChangeListener != null) {
            onStateChangeListener.OnStateChange(currentState);
        }
    }

    public void onError() {
        currentState = STATE.ERROR;
        MediaManager.INSTANCE()
                .releaseMediaPlayer();
        loadingBar.setVisibility(GONE);
        if (onStateChangeListener != null) {
            onStateChangeListener.OnStateChange(currentState);
        }
    }

    public void onCompletion() {
        System.gc();
        currentState = STATE.COMPLETE;
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        MediaManager.INSTANCE()
                .releaseMediaPlayer();
        VideoUtil.saveProgress(getContext(), source.toString(), 0);
        if (onStateChangeListener != null) {
            onStateChangeListener.OnStateChange(currentState);
        }
    }


    public void onRelease() {
        if (currentState == STATE.PAUSE || currentState == STATE.PLAYING) {
            long position = MediaManager.INSTANCE()
                    .getCurrentPosition();
            VideoUtil.saveProgress(getContext(), source.toString(), position);
        }
        currentState = STATE.NORMAL;
        MediaManager.INSTANCE()
                .releaseMediaPlayer();
        removeTextureView();
        AudioManager mAudioManager = (AudioManager) getContext().getSystemService(Context
                .AUDIO_SERVICE);
        if (mAudioManager != null) {
            mAudioManager.abandonAudioFocus(ListVideoPlayerManager.onAudioFocusChangeListener);
        }
        VideoUtil.scanForActivity(getContext())
                .getWindow()
                .clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        MediaManager.INSTANCE()
                .release();
        if (onStateChangeListener != null) {
            onStateChangeListener.OnStateChange(currentState);
        }
    }

    private void initTextureView(ScaleType scaleType) {
        MediaManager.INSTANCE()
                .initTextureView(getContext(),scaleType);
    }


    public void addTextureView() {
        LayoutParams layoutParams = new LayoutParams(ViewGroup
                .LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        textureContainer.addView(MediaManager.INSTANCE().textureView, layoutParams);
    }

    public void addSeekBar() {
        mControllerView = LayoutInflater.from(getContext())
                .inflate(R.layout.tx_video_palyer_controller, null, false);
        mControllerView.findViewById(R.id.full_screen).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                enterFullScreen();
            }

        });
        mControllerView.findViewById(R.id.back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                exitFullScreen();
            }
        });
        mSeekBar = mControllerView.findViewById(R.id.seek);
        mPositionTimeTv = mControllerView.findViewById(R.id.position);
        mDurationTimeTv = mControllerView.findViewById(R.id.length);
        mSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        handler = new UpdateTimeHandler(mSeekBar , mPositionTimeTv ,mDurationTimeTv );
        LayoutParams layoutParams = new LayoutParams(ViewGroup
                .LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        mControllerView.setVisibility(GONE);
        textureContainer.addView(mControllerView, layoutParams);

    }

    public void hideController() {
        if (mControllerView != null) {
            mControllerView.setVisibility(GONE);
        }
    }

    public void showController() {
        if (mControllerView != null) {
            mControllerView.setVisibility(VISIBLE);
        }
    }


    public void removeTextureView() {
        textureContainer.removeView(MediaManager.INSTANCE().textureView);
    }

    public boolean isCurrentVideo() {
        return ListVideoPlayerManager.getCurrentVideo() != null && ListVideoPlayerManager
                .getCurrentVideo() == this;
    }

    public int getCurrentState() {
        return currentState;
    }

    public int getCurrentMode() {
        return currentMode;
    }

    public void setOnStateChangeListener(OnStateChangeListener onStateChangeListener) {
        this.onStateChangeListener = onStateChangeListener;
        this.onStateChangeListener.OnStateChange(currentState);
    }

    public void setOnModeChangeListener(OnModeChangeListener onModeChangeListener) {
        this.onModeChangeListener = onModeChangeListener;
        this.onModeChangeListener.OnModeChange(currentMode);
    }

    public interface OnStateChangeListener {

        void OnStateChange(int state);
    }

    public interface OnModeChangeListener {
        void OnModeChange(int mode);
    }

    public interface OnBufferingUpdateListener {


        void onBufferingUpdate(int state);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (isCurrentVideo()) {
            ListVideoPlayerManager.releaseAllVideos();
        }
    }

    public void onBufferingUpdate(int percent){
        if(onBufferingUpdateListener!=null){
            onBufferingUpdateListener.onBufferingUpdate(percent);
        }
    }

    public void setOnBufferingUpdateListener(OnBufferingUpdateListener onBufferingUpdateListener) {
        this.onBufferingUpdateListener = onBufferingUpdateListener;
    }

    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar
            .OnSeekBarChangeListener() {
        //拖动的过程中调用
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        //开始拖动的时候调用
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            //暂停视频的播放、停止时间和进度条的更新
            MediaManager.INSTANCE()
                    .pause();
        }

        //停止拖动时调用
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            //把视频跳转到对应的位置
            int progress = seekBar.getProgress();
            int duration = (int) MediaManager.INSTANCE()
                    .getDuration();
            int position = duration * progress / 100;
            MediaManager.INSTANCE()
                    .seekTo(position);
            MediaManager.INSTANCE()
                    .start();
            updatePlayTimeAndProgress();
        }
    };

    //更新播放的时间和进度
    public void updatePlayTimeAndProgress() {
        //获取目前播放的进度
        if (mSeekBar.getVisibility() == View.GONE) {
            mSeekBar.setVisibility(View.VISIBLE);
        }
        int currentPosition = MediaManager.INSTANCE()
                .getCurrentPosition();
        Log.d("jingtian", "当前进度是------》" + currentPosition + "");

        //更新进度
        long duration = MediaManager.INSTANCE()
                .getDuration();
        if (duration == 0) {
            return;
        }
        long progress = 100 * currentPosition / duration;
        mSeekBar.setProgress((int) progress);
        mPositionTimeTv.setText(VideoUtil.formatTime(currentPosition));
        mDurationTimeTv.setText(VideoUtil.formatTime(duration));
        //发送一个更新的延时消息
        handler.sendEmptyMessageDelayed(MSG_UPDATE_TIME_PROGRESS, 500);
    }

    private static class UpdateTimeHandler extends Handler {

        private WeakReference<SeekBar> seekBarWeakReference;
        private WeakReference<TextView> positionTimeTvWf;
        private WeakReference<TextView> durationTimeTvWf;

        UpdateTimeHandler(SeekBar seekBar , TextView positionTv , TextView durationTimeTv) {
            seekBarWeakReference = new WeakReference<>(seekBar);
            positionTimeTvWf = new WeakReference<>(positionTv);
            durationTimeTvWf = new WeakReference<>(durationTimeTv);
        }


        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_UPDATE_TIME_PROGRESS:
                    SeekBar seekBar = seekBarWeakReference.get();
                    TextView positionTimeTv = positionTimeTvWf.get();
                    TextView durationTimeTv = durationTimeTvWf.get();
                    if (seekBar == null) {
                        return;
                    }
                    if (seekBar.getVisibility() == View.GONE) {
                        seekBar.setVisibility(View.VISIBLE);
                    }
                    int currentPosition = MediaManager.INSTANCE()
                            .getCurrentPosition();
                    Log.d("jingtian", "当前进度是------》handleMessage" + currentPosition + "");

                    //更新进度
                    long duration = MediaManager.INSTANCE()
                            .getDuration();
                    if (duration == 0) {
                        return;
                    }
                    long progress = 100 * currentPosition / duration;
                    seekBar.setProgress((int) progress);
                    positionTimeTv.setText(VideoUtil.formatTime(currentPosition));
                    durationTimeTv.setText(VideoUtil.formatTime(duration));
                    //发送一个更新的延时消息
                    this.sendEmptyMessageDelayed(MSG_UPDATE_TIME_PROGRESS, 500);
                    break;
            }
        }
    }
}
