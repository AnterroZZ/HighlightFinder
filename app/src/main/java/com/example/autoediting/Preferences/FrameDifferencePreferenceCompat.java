package com.example.autoediting.Preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;

import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceManager;

import com.example.autoediting.R;
import com.example.autoediting.Utilities;

public class FrameDifferencePreferenceCompat extends PreferenceDialogFragmentCompat {

    private NumberPicker mPicker;
    private static final int MIN_VALUE = 1;
    private static final int MAX_VALUE = 9;
    public static final boolean WRAP_SELECTOR_WHEEL = true;

    private int value;

    private SharedPreferences mSharedPreferences;



    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mPicker = view.findViewById(R.id.picker);
        mPicker.setMinValue(MIN_VALUE);
        mPicker.setMaxValue(MAX_VALUE);
        mPicker.setWrapSelectorWheel(WRAP_SELECTOR_WHEEL);
        mPicker.setDisplayedValues( new String[] {"0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9"});
        mPicker.setValue((int)(mSharedPreferences.getFloat(Utilities.SHARED_FRAME_DIFFERENCE,  (float)0.1)*10));
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if(positiveResult)
        {
            float value = mPicker.getValue();
            value = value /10;
            mSharedPreferences.edit().putFloat(Utilities.SHARED_FRAME_DIFFERENCE,value).apply();
        }
    }
    public static FrameDifferencePreferenceCompat newInstance(String key)
    {
        final FrameDifferencePreferenceCompat fragment = new FrameDifferencePreferenceCompat();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);

        return fragment;
    }




}
