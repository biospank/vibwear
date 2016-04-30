package it.vibwear.app.audio;

import android.util.Log;

import it.vibwear.app.audio.util.AudioUtil;
import it.vibwear.app.utils.AudioPreference;

/**
 * Created by biospank on 13/04/15.
 */
public class LoudNoiseDetector implements AudioClipListener
{
    private static final String TAG = "LoudNoiseDetector";

    private double volumeThreshold;

    private AudioPreference audioPref;

    public static final int DEFAULT_LOUDNESS_THRESHOLD = 1000;

    private static final boolean DEBUG = true;

    public LoudNoiseDetector()
    {
        volumeThreshold = DEFAULT_LOUDNESS_THRESHOLD;
    }

    public LoudNoiseDetector(double volumeThreshold)
    {
        this.volumeThreshold = volumeThreshold;
    }

    public LoudNoiseDetector(AudioPreference pref)
    {
        this.audioPref = pref;
    }

//    @Override
//    public boolean heard(short[] data, int sampleRate)
//    {
//        boolean heard = false;
//        // use rms to take the entire audio signal into account
//        // and discount any one single high amplitude
//        double currentVolume = rootMeanSquared(data);
//        if (DEBUG)
//        {
//            Log.d(TAG, "current: " + currentVolume + " threshold: "
//                    + volumeThreshold);
//        }
//
//        if (currentVolume > volumeThreshold)
//        {
//            Log.d(TAG, "heard");
//            heard = true;
//        }
//
//        return heard;
//    }

    @Override
    public boolean heard(short[] data, int sampleRate)
    {
        boolean heard = false;
        // use rms to take the entire audio signal into account
        // and discount any one single high amplitude
        double currentVolume = AudioUtil.rootMeanSquared(data);

        if (DEBUG)
        {
            Log.d(TAG, "current: " + currentVolume + " threshold: "
                    + audioPref.getThreshold());
        }

        if (currentVolume > audioPref.getThreshold())
        {
            Log.d(TAG, "heard");
            heard = true;
        }

        return heard;
    }

}
