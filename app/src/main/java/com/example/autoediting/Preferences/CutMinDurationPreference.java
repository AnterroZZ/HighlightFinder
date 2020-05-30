package com.example.autoediting.Preferences;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

import com.example.autoediting.R;

public class CutMinDurationPreference extends DialogPreference {

    public CutMinDurationPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CutMinDurationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public int getDialogLayoutResource() {
        return R.layout.number_picker_dialog;
    }
}
