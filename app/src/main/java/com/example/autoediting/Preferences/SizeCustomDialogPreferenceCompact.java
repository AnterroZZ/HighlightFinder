package com.example.autoediting.Preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceManager;

import com.example.autoediting.R;
import com.example.autoediting.Utilities;

public class SizeCustomDialogPreferenceCompact extends PreferenceDialogFragmentCompat {

    private RadioButton mCompatButton;
    private RadioButton mHDButton;
    private RadioButton mFULLHDButton;
    private RadioButton mCustomButton;
    private EditText mEditWidth;
    private EditText mEditHeight;

    private RadioGroup mRadioGroup;


    private SharedPreferences mSharedPreferences;

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        mCompatButton = view.findViewById(R.id.size_preference_compact);
        mHDButton = view.findViewById(R.id.size_preference_hd);
        mFULLHDButton = view.findViewById(R.id.size_preference_fullhd);
        mCustomButton = view.findViewById(R.id.size_preference_custom);
        mRadioGroup = view.findViewById(R.id.radioGroup);

        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.size_preference_custom )
                {
                    mEditWidth.setVisibility(View.VISIBLE);
                    mEditHeight.setVisibility(View.VISIBLE);
                    mEditHeight.setEnabled(true);
                    mEditWidth.setEnabled(true);
                }else{
                    mEditHeight.setEnabled(false);
                    mEditWidth.setEnabled(false);
                    mEditWidth.setVisibility(View.INVISIBLE);
                    mEditHeight.setVisibility(View.INVISIBLE);
                }
            }
        });

        mEditWidth = view.findViewById(R.id.size_preference_width);
        mEditHeight = view.findViewById(R.id.size_preference_height);

        mEditHeight.setEnabled(false);
        mEditWidth.setEnabled(false);
        mEditWidth.setVisibility(View.INVISIBLE);
        mEditHeight.setVisibility(View.INVISIBLE);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if(positiveResult)
        {
            if(mCompatButton.isChecked())
            {
                mSharedPreferences.edit().putInt(Utilities.SHARED_SIZE_WIDTH, 854).apply();
                mSharedPreferences.edit().putInt(Utilities.SHARED_SIZE_HEIGHT, 480).apply();
            }else if(mHDButton.isChecked())
            {
                mSharedPreferences.edit().putInt(Utilities.SHARED_SIZE_WIDTH, 1280).apply();
                mSharedPreferences.edit().putInt(Utilities.SHARED_SIZE_HEIGHT, 720).apply();
            }else if(mFULLHDButton.isChecked())
            {
                mSharedPreferences.edit().putInt(Utilities.SHARED_SIZE_WIDTH, 1920).apply();
                mSharedPreferences.edit().putInt(Utilities.SHARED_SIZE_HEIGHT, 1080).apply();
            }else if (mCustomButton.isChecked())
            {
                if(CorrectEditTextValues())
                {
                    int width = Integer.valueOf(mEditWidth.getText().toString());
                    int height = Integer.valueOf(mEditHeight.getText().toString());

                    mSharedPreferences.edit().putInt(Utilities.SHARED_SIZE_WIDTH, width).apply();
                    mSharedPreferences.edit().putInt(Utilities.SHARED_SIZE_HEIGHT, height).apply();
                }
            }



        }
    }

    private boolean CorrectEditTextValues() {
        String widthString = mEditWidth.getText().toString();
        String heightString = mEditHeight.getText().toString();
        int width;
        int height;
        if(widthString.equals("") || heightString.equals(""))
        {
            width = 0;
            height = 0;
        } else
        {
            width = Integer.valueOf(widthString);
            height = Integer.valueOf(heightString);
        }

        if(width == 0 || height == 0)
        {
            Toast.makeText(getContext(), "Width or Height can't be 0!", Toast.LENGTH_LONG).show();
            return false;
        }else if(width > 2600 )
        {
            Toast.makeText(getContext(), "Width can't be higher than 2600", Toast.LENGTH_LONG).show();
            return false;
        }else if (height > 1500) {
            Toast.makeText(getContext(), "Height can't be higher than 1500", Toast.LENGTH_LONG).show();
            return false;
        }else return true;
    }

    public static SizeCustomDialogPreferenceCompact newInstance(String key)
    {
        final SizeCustomDialogPreferenceCompact fragment = new SizeCustomDialogPreferenceCompact();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);

        return fragment;
    }
}
