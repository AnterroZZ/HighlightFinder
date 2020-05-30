package com.example.autoediting.Preferences;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RadioButton;

import androidx.preference.DialogPreference;

import com.example.autoediting.R;

public class SizeCustomDialogPreference extends DialogPreference {


    public SizeCustomDialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.style.PreferenceDialogTheme);
    }

    public SizeCustomDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.dialogPreferenceStyle);
    }

    @Override
    public int getDialogLayoutResource() {
        return R.layout.preference_size_dialog;
    }
}
