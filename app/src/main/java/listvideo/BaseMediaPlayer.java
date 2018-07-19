package listvideo;

import android.net.Uri;
import android.view.Surface;

public abstract class BaseMediaPlayer {

    public Uri source;

    public abstract void start();

    public abstract void prepare();

    public abstract void pause();

    public abstract boolean isPlaying();

    public abstract void seekTo(int time);

    public abstract void release();

    public abstract int getCurrentPosition();

    public abstract int getDuration();

    public abstract void setSurface(Surface surface);

    public abstract void setVolume(float leftVolume, float rightVolume);

}
