package it.vibwear.app.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import it.lampwireless.vibwear.app.R;

/**
 * Created by biospank on 13/04/15.
 */
public class AudioPreference implements MicPreference, VibrationPreference, SwitchPreference {

    public static final String AUDIO_PREFS_NAME = "AUDIO_DETAILS";
    public static final String AUDIO_TRESHOLD = "audio_treshold";
    public static final String AUDIO_VIB_TIME = "audio_vib_time";
    public static final String AUDIO_KEY_PREF = "pref_key_audio";

    protected Context context;

    public AudioPreference(Context context) {
        super();
        this.context = context;
    }

    public int getVibrationTime() {
        SharedPreferences settings = context.getSharedPreferences(AUDIO_PREFS_NAME,
                Context.MODE_PRIVATE);
        return settings.getInt(AUDIO_VIB_TIME, DEFAULT_VIB_TIME);

    }

    public void setVibrationTime(int progress) {
        SharedPreferences.Editor editor;
        SharedPreferences settings = context.getSharedPreferences(AUDIO_PREFS_NAME,
                Context.MODE_PRIVATE);
        editor = settings.edit();

        editor.putInt(AUDIO_VIB_TIME, progress);

        editor.commit();

    }

    public int getThreshold() {
        SharedPreferences settings = context.getSharedPreferences(AUDIO_PREFS_NAME,
                Context.MODE_PRIVATE);
        return settings.getInt(AUDIO_TRESHOLD, DEFAULT_TRESHOLD);

    }

    public void setThreshold(int progress) {
        SharedPreferences.Editor editor;
        SharedPreferences settings = context.getSharedPreferences(AUDIO_PREFS_NAME,
                Context.MODE_PRIVATE);
        editor = settings.edit();

        editor.putInt(AUDIO_TRESHOLD, progress);

        editor.commit();

    }

    @Override
    public boolean switchState() {
        SharedPreferences.Editor editor;
        boolean active;
        SharedPreferences settings = context.getSharedPreferences(AUDIO_PREFS_NAME,
                Context.MODE_PRIVATE);
        editor = settings.edit();

        if(settings.getBoolean(AUDIO_KEY_PREF, false)) {
            editor.putBoolean(AUDIO_KEY_PREF, false);
            active = false;

        } else {
            editor.putBoolean(AUDIO_KEY_PREF, true);
            active = true;

        }

        editor.apply();

        return active;

    }

    @Override
    public boolean getState() {
        SharedPreferences settings = context.getSharedPreferences(AUDIO_PREFS_NAME,
                Context.MODE_PRIVATE);
        return settings.getBoolean(AUDIO_KEY_PREF, false);

    }

    @Override
    public String getLabel() {
        SharedPreferences settings = context.getSharedPreferences(AUDIO_PREFS_NAME,
                Context.MODE_PRIVATE);
        if(settings.getBoolean(AUDIO_KEY_PREF, false))
            return context.getString(R.string.activeAudioServiceDesc);
        else
            return context.getString(R.string.audioServiceDesc);
    }

    @Override
    public int getImage() {
        SharedPreferences settings = context.getSharedPreferences(AUDIO_PREFS_NAME,
                Context.MODE_PRIVATE);
        if(settings.getBoolean(AUDIO_KEY_PREF, false))
            return R.drawable.ic_audio_active;
        else
            return R.drawable.ic_audio;
    }

}
