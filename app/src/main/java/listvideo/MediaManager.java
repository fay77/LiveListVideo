package listvideo;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;

public class MediaManager implements TextureView.SurfaceTextureListener {

    private static final String TAG = "MediaManager";

    private static final int HANDLER_PREPARE = 0;
    private static final int HANDLER_RELEASE = 2;

    public ResizeTextureView textureView;
    private SurfaceTexture savedSurfaceTexture;
    private Surface surface;

    private MediaHandler mMediaHandler;
    public Handler mainThreadHandler = new Handler();

    private BaseMediaPlayer mediaPlayer;

    private static MediaManager mediaManager;

    private MediaManager() {
        HandlerThread mMediaHandlerThread = new HandlerThread(TAG);
        mMediaHandlerThread.start();
        mMediaHandler = new MediaHandler(mMediaHandlerThread.getLooper());
        if (mediaPlayer == null) {
            mediaPlayer = new SystemMediaPlayer();
        }
    }

    public static MediaManager INSTANCE() {
        if (mediaManager == null) {
            mediaManager = new MediaManager();
        }
        return mediaManager;
    }

    public void setSource(Uri source) {
        this.mediaPlayer.source = source;
    }

    public Uri getSource() {
        return this.mediaPlayer.source;
    }

    public void initTextureView(Context context, ListVideoPlayer.ScaleType scaleType) {
        removeTextureView();
        textureView = new ResizeTextureView(context);
        textureView.setScaleType(scaleType);
        textureView.setSurfaceTextureListener(this);
    }

    public void removeTextureView() {
        if (savedSurfaceTexture != null) {
            savedSurfaceTexture.release();
            savedSurfaceTexture = null;
        }
        if (textureView != null && textureView.getParent() != null) {
            ((ViewGroup) textureView.getParent()).removeView(textureView);
        }
    }

    public void releaseMediaPlayer() {
        mMediaHandler.removeCallbacksAndMessages(null);
        Message msg = new Message();
        msg.what = HANDLER_RELEASE;
        mMediaHandler.sendMessage(msg);
    }

    private void prepare() {
        releaseMediaPlayer();
        Message msg = new Message();
        msg.what = HANDLER_PREPARE;
        mMediaHandler.sendMessage(msg);
    }

    public void pause() {
        mediaPlayer.pause();
    }

    public void start() {
        mediaPlayer.start();
    }

    public void isPlaying() {
        mediaPlayer.isPlaying();
    }

    public void seekTo(int time) {
        mediaPlayer.seekTo(time);
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public long getDuration() {
        return mediaPlayer.getDuration();
    }

    public void release() {
        if (surface != null) {
            surface.release();
        }
        removeTextureView();
        if (textureView != null) {
            textureView.setSurfaceTextureListener(null);
            textureView = null;
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        if (savedSurfaceTexture == null) {
            savedSurfaceTexture = surfaceTexture;
            prepare();
        } else {
            textureView.setSurfaceTexture(savedSurfaceTexture);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int w, int h) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return savedSurfaceTexture == null;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }


    class MediaHandler extends Handler {
        MediaHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLER_PREPARE:
                    mediaPlayer.prepare();
                    if (savedSurfaceTexture != null) {
                        if (surface != null) {
                            surface.release();
                        }
                        surface = new Surface(savedSurfaceTexture);
                        mediaPlayer.setSurface(surface);
                    }
                    break;
                case HANDLER_RELEASE:
                    mediaPlayer.release();
                    break;
            }
        }
    }
}
