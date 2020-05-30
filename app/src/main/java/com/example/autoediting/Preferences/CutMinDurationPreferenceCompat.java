package com.example.autoediting.Preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;

import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceManager;

import com.example.autoediting.R;
import com.example.autoediting.Utilities;

public class CutMinDurationPreferenceCompat extends PreferenceDialogFragmentCompat {

    private NumberPicker mPicker;
    private static final int MIN_VALUE = 1;
    private static final int MAX_VALUE = 20;
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

    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if(positiveResult)
        {
            value = mPicker.getValue();
            mSharedPreferences.edit().putInt(Utilities.SHARED_ONE_CUT_DURATION, value).apply();
        }
    }

    public static CutMinDurationPreferenceCompat newInstance(String key)
    {
        final CutMinDurationPreferenceCompat fragment = new CutMinDurationPreferenceCompat();
        final Bundle bundle = new Bundle(1);
        bundle.putString(ARG_KEY, key);
        fragment.setArguments(bundle);

        return fragment;
    }
}
