package it.vibwear.app.audio;

import android.util.Log;

import it.vibwear.app.audio.util.AudioUtil;
import it.vibwear.app.utils.AudioPreference;

/**
 * alternative Loud Noise detector that tracks the difference between
 * the new noise and an averagre value. It might be useful in some situations.
 *
 */
public class LoudNoiseDetectorAboveNormal implements AudioClipListener {
    private static final String TAG = "MultipleClapDetector";

    private AudioPreference audioPref;

    private double averageVolume;

    private double lowPassAlpha = 0.5;

    private double STARTING_AVERAGE = 100.0;

    private double INCREASE_FACTOR = 100.0;

    private static final boolean DEBUG = true;

    public LoudNoiseDetectorAboveNormal()
    {
        averageVolume = STARTING_AVERAGE;
    }

    public LoudNoiseDetectorAboveNormal(AudioPreference pref)
    {
        this.audioPref = pref;
        this.averageVolume = STARTING_AVERAGE;
    }

    @Override
    public boolean heard(short[] data, int sampleRate) {
        boolean heard = false;
        // use rms to take the entire audio signal into account
        // and discount any one single high amplitude
        double currentVolume = AudioUtil.rootMeanSquared(data);

        double volumeThreshold = audioPref.getThreshold() * INCREASE_FACTOR;

        if (DEBUG) {
            Log.d(TAG, "current: " + currentVolume + " avg: " + averageVolume
                    + " threshold: " + volumeThreshold);
        }

        if (currentVolume > volumeThreshold) {
            Log.d(TAG, "heard");
            heard = true;
        }

        return heard;
    }

    private double lowPass(double current, double last) {
        return last * (1.0 - lowPassAlpha) + current * lowPassAlpha;
    }

//    private static final String TAG = "MultipleClapDetector";
//
//    private double averageVolume;
//
//    private double lowPassAlpha = 0.5;
//
//    private double STARTING_AVERAGE = 100.0;
//
//    private double INCREASE_FACTOR = 100.0;
//
//    private static final boolean DEBUG = true;
//
//    public LoudNoiseDetectorAboveNormal()
//    {
//        averageVolume = STARTING_AVERAGE;
//    }
//
//    @Override
//    public boolean heard(short[] data, int sampleRate) {
//        boolean heard = false;
//        // use rms to take the entire audio signal into account
//        // and discount any one single high amplitude
//        double currentVolume = rootMeanSquared(data);
//
//        double volumeThreshold = averageVolume * INCREASE_FACTOR;
//
//        if (DEBUG) {
//            Log.d(TAG, "current: " + currentVolume + " avg: " + averageVolume
//                    + " threshold: " + volumeThreshold);
//        }
//
//        if (currentVolume > volumeThreshold) {
//            Log.d(TAG, "heard");
//            heard = true;
//        } else {
//            // Big changes should have very little affect on
//            // the average value but if the average volume does increase
//            // consistently let the average increase too
//            averageVolume = lowPass(currentVolume, averageVolume);
//        }
//
//        return heard;
//    }
//
//    private double lowPass(double current, double last) {
//        return last * (1.0 - lowPassAlpha) + current * lowPassAlpha;
//    }
//
//    private double rootMeanSquared(short[] nums) {
//        double ms = 0;
//
//        for (int i = 0; i < nums.length; i++) {
//            ms += nums[i] * nums[i];
//        }
//
//        ms /= nums.length;
//
//        return Math.sqrt(ms);
//    }
}
