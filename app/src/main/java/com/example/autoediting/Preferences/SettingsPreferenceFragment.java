package com.example.autoediting.Preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.example.autoediting.R;
import com.example.autoediting.Utilities;

public class SettingsPreferenceFragment extends PreferenceFragmentCompat {

    private SizeCustomDialogPreference mSizePreference;
    private FrameDifferencePreference mDifferencePreference;
    private SampleDurationPreference mDurationPreference;
    private CutMinDurationPreference mCutMinPreference;

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener mPreferencesChangeListener;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_preference_screen, rootKey);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        mSizePreference = findPreference("size");
        mDifferencePreference = findPreference("difference");
        mDurationPreference = findPreference("duration");
        mCutMinPreference = findPreference("one_cut");


        mSizePreference.setSummary("Frame size used for calculation: " + mSharedPreferences.getInt(Utilities.SHARED_SIZE_WIDTH,1280) + "x" + mSharedPreferences.getInt(Utilities.SHARED_SIZE_HEIGHT,720));
        mDifferencePreference.setSummary("Frames difference: " + mSharedPreferences.getFloat(Utilities.SHARED_FRAME_DIFFERENCE,(float)0.1));
        mDurationPreference.setSummary("Duration of sample video: " + mSharedPreferences.getInt(Utilities.SHARED_SAMPLE_DURATION,1) + " min");
        mCutMinPreference.setSummary("Minimal time of video cut: " + mSharedPreferences.getInt(Utilities.SHARED_ONE_CUT_DURATION, 1) + " seconds");



        mPreferencesChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                switch (key)
                {
                    //Because it gets set up as second
                    case Utilities.SHARED_SIZE_HEIGHT:
                        setUpSizePreference(sharedPreferences, "size");
                        break;
                    case Utilities.SHARED_SAMPLE_DURATION:
                        setUpDurationPreference(sharedPreferences,key);
                        break;
                    case Utilities.SHARED_FRAME_DIFFERENCE:
                        setUpDifferencePreference(sharedPreferences, key);
                        break;
                    case Utilities.SHARED_ONE_CUT_DURATION:
                        setUpCutMinDuration(sharedPreferences, key);
                        break;

                }
            }
        };

    }

    private void setUpCutMinDuration(SharedPreferences sharedPreferences, String key) {
        int cutMinDuration = sharedPreferences.getInt(key, 1);
        mCutMinPreference.setSummary("Minimal time of video cut: " + cutMinDuration + " seconds");

    }

    private void setUpDifferencePreference(SharedPreferences sharedPreferences, String key) {
        float difference = sharedPreferences.getFloat(key, (float)0.1);
        mDifferencePreference.setSummary("Frames difference: " + difference);
    }

    private void setUpDurationPreference(SharedPreferences sharedPreferences, String key) {
        int duration = sharedPreferences.getInt(key, 1);
        mDurationPreference.setSummary("Duration of sample video: " + duration + " min");
    }


    private void setUpSizePreference(SharedPreferences sharedPreferences, String key) {
        int width = sharedPreferences.getInt(Utilities.SHARED_SIZE_WIDTH, 1280);
        int height = sharedPreferences.getInt(Utilities.SHARED_SIZE_HEIGHT, 720);
        mSizePreference.setSummary("Frame size used for calculation: " + width + "x" + height);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setDivider(ContextCompat.getDrawable(getActivity(), R.drawable.divider));
        setDividerHeight(1);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if(preference instanceof SizeCustomDialogPreference)
        {
            DialogFragment dialogFragment = SizeCustomDialogPreferenceCompact.newInstance(preference.getKey());
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getFragmentManager(), null);
        } else if(preference instanceof SampleDurationPreference)
        {
            DialogFragment dialogFragment = SampleDurationPreferenceCompat.newInstance(preference.getKey());
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getFragmentManager(), null);
        }else if(preference instanceof FrameDifferencePreference) {
            DialogFragment dialogFragment = FrameDifferencePreferenceCompat.newInstance(preference.getKey());
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getFragmentManager(), null);
        }else if(preference instanceof CutMinDurationPreference) {
            DialogFragment dialogFragment = CutMinDurationPreferenceCompat.newInstance(preference.getKey());
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getFragmentManager(), null);
        }else super.onDisplayPreferenceDialog(preference);
    }

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(mPreferencesChangeListener);
    }

    @Override
    public void onPause() {
        super.onPause();

        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(mPreferencesChangeListener);
    }
}
