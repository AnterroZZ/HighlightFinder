package com.example.autoediting.Preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;

import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceManager;

import com.example.autoediting.R;
import com.example.autoediting.Utilities;

public class SampleDurationPreferenceCompat extends PreferenceDialogFragmentCompat {

    private NumberPicker mPicker;
    private static final int MIN_VALUE = 1;
    private static final int MAX_VALUE = 50;
    public static final boolean WRAP_SELECTOR_WHEEL = true;

    private int value;

    private SharedPreferences mSharedPreferences;


    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        mPicker = view.findViewById(R.id.picker);
        mPicker.setMinValue(MIN_VALUE);
        mPicker.setMaxValue(MAX_VALUE);
        mPicker.setWrapSelectorWheel(WRAP_SELECTOR_WHEEL);
        mPicker.setValue(2);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if(positiveResult)
        {
            int value = mPicker.getValue();
            mSharedPreferences.edit().putInt(Utilities.SHARED_SAMPLE_DURATION,value).apply();
        }
    }
    public static SampleDurationPreferenceCompat newInstance(String key)
    {
        final SampleDurationPreferenceCompat fragment = new SampleDurationPreferenceCompat();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);

        return fragment;
    }




}
