package com.cibiod2.estetho.utils;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;

import com.rey.material.widget.Slider;

public class VolumeObserver extends ContentObserver {
    final Context context;
    final Slider slider;

    public VolumeObserver(Context c, Slider s, Handler handler) {
        super(handler);
        context = c;
        slider = s;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);

        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        float currentVolume = (float) audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = (float) audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        slider.setPosition(currentVolume / maxVolume, true);
    }
}
