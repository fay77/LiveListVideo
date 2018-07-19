package listvideo;

import android.annotation.SuppressLint;
import android.media.AudioManager;

public class ListVideoPlayerManager {

    @SuppressLint("StaticFieldLeak")
    private static ListVideoPlayer currentVideoView;


    public static AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new
            AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    try {
                        onPlayPlaying();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    releaseAllVideos();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    try {
                        onPlayPause();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    break;
            }
        }
    };

    public static void setCurrentVideo(ListVideoPlayer videoView) {
        if(currentVideoView!=null&&currentVideoView !=videoView){
            ListVideoPlayerManager.currentVideoView.onRelease();
        }
        ListVideoPlayerManager.currentVideoView = videoView;
    }

    public static ListVideoPlayer getCurrentVideo() {
        return currentVideoView;
    }


    public static void releaseAllVideos() {
        if (currentVideoView != null) {
            currentVideoView.onRelease();
            currentVideoView = null;
        }
    }


    public static void onPlayPlaying() {
        if (currentVideoView != null) {
            if (currentVideoView.getCurrentState() == ListVideoPlayer.STATE.PAUSE) {
                currentVideoView.onPlaying();
            }
        }
    }

    public static void onPlayPause() {
        if (currentVideoView != null) {
            switch (currentVideoView.getCurrentState()) {
                case ListVideoPlayer.STATE.COMPLETE:
                case ListVideoPlayer.STATE.NORMAL:
                case ListVideoPlayer.STATE.ERROR:
                case ListVideoPlayer.STATE.PREPARING:
                    releaseAllVideos();
                    break;
                default:
                    currentVideoView.onPause();
                    break;
            }
        }
    }
}
