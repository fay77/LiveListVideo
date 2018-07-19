package listvideo;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.view.Surface;

public class SystemMediaPlayer extends BaseMediaPlayer implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer
                .OnSeekCompleteListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener,
        MediaPlayer.OnVideoSizeChangedListener {

    private MediaPlayer mediaPlayer;


    public int currentVideoWidth = 0;
    public int currentVideoHeight = 0;

    @Override
    public void start() {
        mediaPlayer.start();
    }

    @Override
    public void prepare() {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setVolume(0,0);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnBufferingUpdateListener(this);
            mediaPlayer.setScreenOnWhilePlaying(true);
            mediaPlayer.setOnSeekCompleteListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnInfoListener(this);
            mediaPlayer.setOnVideoSizeChangedListener(this);
            mediaPlayer.setDataSource(source.toString());
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void pause() {
        if(mediaPlayer!=null&&mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    @Override
    public void seekTo(int time) {
        try {
            mediaPlayer.seekTo(time);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer=null;
        }
    }

    @Override
    public int getCurrentPosition() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        } else {
            return 0;
        }
    }

    @Override
    public int getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        } else {
            return 0;
        }
    }

    @Override
    public void setSurface(Surface surface) {
        mediaPlayer.setSurface(surface);
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        mediaPlayer.setVolume(leftVolume, rightVolume);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, final int i) {
        MediaManager.INSTANCE().mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (ListVideoPlayerManager.getCurrentVideo() != null) {
                    ListVideoPlayerManager.getCurrentVideo()
                            .onBufferingUpdate(i);
                }
            }
        });
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        MediaManager.INSTANCE().mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (ListVideoPlayerManager.getCurrentVideo() != null) {
                    ListVideoPlayerManager.getCurrentVideo()
                            .onCompletion();
                }
            }
        });
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        MediaManager.INSTANCE().mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (ListVideoPlayerManager.getCurrentVideo() != null) {
                    ListVideoPlayerManager.getCurrentVideo()
                            .onError();
                }
            }
        });
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, final int what, final int extra) {
        MediaManager.INSTANCE().mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (ListVideoPlayerManager.getCurrentVideo() != null) {
                    if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                        if (ListVideoPlayerManager.getCurrentVideo()
                                .getCurrentState() == ListVideoPlayer.STATE.PREPARING) {
                            ListVideoPlayerManager.getCurrentVideo()
                                    .onPrepared();
                        }
                    }
                }
            }
        });
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        MediaManager.INSTANCE().mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (ListVideoPlayerManager.getCurrentVideo() != null) {
                    ListVideoPlayerManager.getCurrentVideo()
                            .onPrepared();
                }
            }
        });
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mediaPlayer, int w, int h) {
        currentVideoWidth = w;
        currentVideoHeight = h;
        MediaManager.INSTANCE().mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if(MediaManager.INSTANCE().textureView!=null) {
                    MediaManager.INSTANCE().textureView.setVideoSize(currentVideoWidth,
                            currentVideoHeight);
                }
            }
        });
    }
}
