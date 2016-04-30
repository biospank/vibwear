package it.vibwear.app.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import it.lampwireless.vibwear.app.R;
import it.vibwear.app.utils.AudioPreference;

/**
 * Created by biospank on 05/05/15.
 */
public class MicSliderFragment extends Fragment {

    protected AudioPreference audioPreference;
    protected SeekBar sbAudio;

    public MicSliderFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_mic_slider, container, false);

        sbAudio = (SeekBar)layout.findViewById(R.id.sbAudio);

        sbAudio.setProgress(audioPreference.getThreshold());

        sbAudio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {

                audioPreference.setThreshold(progress);
            }
        });

        return layout;
    }

    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);
        Bundle preference = getArguments();
        String preferenceClassName = preference.getString("preference");

        switch (preferenceClassName) {
            case "AudioPreference":
                this.audioPreference = new AudioPreference(activity);
                break;
        }


    }

}
