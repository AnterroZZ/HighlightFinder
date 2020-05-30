package com.example.autoediting;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.widget.VideoView;

public class CustomVideoPlayer extends VideoView implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private MediaPlayer mediaPlayer;

    public CustomVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.setOnPreparedListener(this);
        this.setOnCompletionListener(this);
        this.setOnErrorListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        this.mediaPlayer = mp;
        mediaPlayer.setLooping(true);
        this.setVolume(0);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    public void mute()
    {
        this.setVolume(0);
    }

    public void unmute()
    {
        this.setVolume(100);
    }
    private void setVolume(int amount)
    {
        final int max = 100;
        final double numerator = max - amount > 0 ? Math.log(max - amount) : 0;
        final float volume = (float) (1 - (numerator / Math.log(max)));

        this.mediaPlayer.setVolume(volume, volume);
    }
}
