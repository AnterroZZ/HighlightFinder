package com.example.autoediting;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;

public class PerformanceActivity extends AppCompatActivity {

    private ImageView mImageView;
    private TextView mValueOfImage;
    private ProgressBar progressBar;
    private float calculatedValue;



    private TextView mTimeCompat;
    private TextView mTimeHD;
    private TextView mTimeFULLHD;

    private ProgressBar mProgressBar1;
    private ProgressBar mProgressBar2;
    private ProgressBar mProgressBar3;


    private TextView mTimeTotalLabel;
    private TextView mTimeTotalValue;


    private int mWhichValue;

    private long startTime;
    private long endTime;
    private long longCalculationTime;
    private float floatCalculationTime;

    private float mCalculatedTimeCompat;
    private float mCalculatedTimeHD;
    private float mCalculatedTimeFULLHD;

    private Bitmap mImageBitmap;
    private final static int COMPAT_WIDTH = 854;
    private final static int COMPAT_HEIGHT = 480;
    private final static int HD_WIDTH = 1280;
    private final static int HD_HEIGHT = 720;
    private final static int FULLHD_WIDTH = 1920;
    private final static int FULLHD_HEIGHT = 1080;


    private SharedPreferences mSharedPreferences;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_performance);

        mImageView = findViewById(R.id.imageView);
        mValueOfImage = findViewById(R.id.imageValue);

        mTimeCompat = findViewById(R.id.oneTimeValueCompat);
        mTimeHD = findViewById(R.id.oneTimeValueHD);
        mTimeFULLHD = findViewById(R.id.oneTimeValueFULLHD);

        mTimeTotalLabel = findViewById(R.id.totalTimeLabel);
        mTimeTotalValue = findViewById(R.id.totalTimeValue);
        mTimeTotalValue.setVisibility(View.INVISIBLE);
        mTimeTotalLabel.setVisibility(View.INVISIBLE);

        mProgressBar1 = findViewById(R.id.progress_bar_1);
        mProgressBar2 = findViewById(R.id.progress_bar_2);
        mProgressBar3 = findViewById(R.id.progress_bar_3);

        mProgressBar1.setVisibility(View.INVISIBLE);
        mProgressBar2.setVisibility(View.INVISIBLE);
        mProgressBar3.setVisibility(View.INVISIBLE);

        mWhichValue = 1;

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mImageBitmap = null;
        Intent intent = getIntent();
        if(intent.getExtras().get(StartingActivity.UPLOADED_IMAGE) != null) {
            Uri image = (Uri) intent.getExtras().get(StartingActivity.UPLOADED_IMAGE);
            try {
                mImageBitmap = Utilities.getThumbnail(image, this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else mImageBitmap = (Bitmap) intent.getExtras().get(StartingActivity.TAKEN_IMAGE);

        mImageView.setImageBitmap(mImageBitmap);

        new BitmapToSingleValue().execute(mImageBitmap);


    }


    /**Simple method for converting milliSeconds to Seconds
     *
     * @param floatCalculationTime Float Value of milliSeconds
     * @return Should return seconds
     */
    private float milisToSec(float floatCalculationTime) {
        float modulo = floatCalculationTime % 10;
        return (floatCalculationTime - modulo) / 1000;
    }

    //Async task used for converting each frame to a single unique value ranged from 0 to 1
    private class BitmapToSingleValue extends AsyncTask <Bitmap, Integer, Float>
    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            startTime = System.currentTimeMillis();
            switch (mWhichValue)
            {
                case 1:
                    mProgressBar1.setVisibility(View.VISIBLE);
                    mTimeCompat.setVisibility(View.INVISIBLE);
                    mProgressBar1.setMax(100);
                    mValueOfImage.setVisibility(View.INVISIBLE);
                    break;
                case 2:
                    mProgressBar2.setVisibility(View.VISIBLE);
                    mTimeHD.setVisibility(View.INVISIBLE);
                    mProgressBar2.setMax(100);
                    break;
                case 3:
                    mProgressBar3.setVisibility(View.VISIBLE);
                    mTimeFULLHD.setVisibility(View.INVISIBLE);
                    mProgressBar2.setMax(100);
                    break;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            switch (mWhichValue)
            {
                case 1:
                    mProgressBar1.setProgress(values[0]);
                    break;
                case 2:
                    mProgressBar2.setProgress(values[0]);
                    break;
                case 3:
                    mProgressBar3.setProgress(values[0]);
                    break;
            }
        }

        @Override
        protected Float doInBackground(Bitmap... bitmaps) {
            float sum = 0;
            float size = 0;
            Bitmap rescaledForCalculations = null;
            int width=1;
            int height=1;
            switch (mWhichValue)
            {
                case 1:
                    rescaledForCalculations = Bitmap.createScaledBitmap(bitmaps[0], COMPAT_WIDTH,COMPAT_HEIGHT, false);
                    width = COMPAT_WIDTH;
                    height = COMPAT_HEIGHT;
                    break;
                case 2:
                    rescaledForCalculations = Bitmap.createScaledBitmap(bitmaps[0], HD_WIDTH, HD_HEIGHT, false);
                    width = HD_WIDTH;
                    height = HD_HEIGHT;
                    break;
                case 3:
                    rescaledForCalculations = Bitmap.createScaledBitmap(bitmaps[0], FULLHD_WIDTH, FULLHD_HEIGHT, false);
                    width = FULLHD_WIDTH;
                    height = FULLHD_HEIGHT;
                    break;
            }
            for(int x = 0; x< width; x++)
            {
                for(int y=0; y<height; y++)
                {
                    int pixel = rescaledForCalculations.getPixel(x,y);
                    float redValue = Color.red(pixel) / 255;
                    float blueValue = Color.blue(pixel) / 255;
                    float greenValue = Color.green(pixel) / 255;
                    float pixelValue = (redValue + blueValue + greenValue) / 3;
                    sum += pixelValue;
                    size++;
                }
                publishProgress(x * 100 / width );
            }

            return sum/size;
        }



        @Override
        protected void onPostExecute(Float aFloat) {
            endTime = System.currentTimeMillis();
            longCalculationTime = endTime-startTime;
            floatCalculationTime = longCalculationTime;
            calculatedValue = aFloat;
            switch (mWhichValue)
            {
                case 1:
                    mProgressBar1.setVisibility(View.INVISIBLE);
                    mValueOfImage.setText(String.valueOf(calculatedValue));
                    mValueOfImage.setVisibility(View.VISIBLE);
                    mTimeCompat.setVisibility(View.VISIBLE);
                    mTimeCompat.setText(String.valueOf(milisToSec(floatCalculationTime)));
                    mCalculatedTimeCompat = floatCalculationTime;


                    mWhichValue = 2;
                    new BitmapToSingleValue().execute(mImageBitmap);
                    break;
                case 2:
                    mProgressBar2.setVisibility(View.INVISIBLE);
                    mTimeHD.setVisibility(View.VISIBLE);
                    mTimeHD.setText(String.valueOf(milisToSec(floatCalculationTime)));
                    mCalculatedTimeHD = floatCalculationTime;

                    mWhichValue = 3;
                    new BitmapToSingleValue().execute(mImageBitmap);
                    break;
                case 3:
                    mProgressBar3.setVisibility(View.INVISIBLE);
                    mTimeFULLHD.setVisibility(View.VISIBLE);
                    mTimeFULLHD.setText(String.valueOf(milisToSec(floatCalculationTime)));

                    int minuteValue = mSharedPreferences.getInt(Utilities.SHARED_SAMPLE_DURATION, 1);
                    int videoWidth = mSharedPreferences.getInt(Utilities.SHARED_SIZE_WIDTH, 1);
                    int videoHeight = mSharedPreferences.getInt(Utilities.SHARED_SIZE_HEIGHT, 1);
                    mCalculatedTimeFULLHD = floatCalculationTime;

                    mTimeTotalValue.setVisibility(View.VISIBLE);
                    mTimeTotalLabel.setVisibility(View.VISIBLE);
                    mTimeTotalLabel.setText("Time for " + minuteValue + " minutes of footage at: " + videoWidth + "x" + videoHeight );
                    if(videoWidth>1280)
                    {
                        floatCalculationTime = mCalculatedTimeFULLHD;
                    }else if(videoWidth>900)
                    {
                        floatCalculationTime = mCalculatedTimeHD;
                    }else
                    {
                        floatCalculationTime = mCalculatedTimeCompat;
                    }

                    mTimeTotalValue.setText(String.valueOf(milisToSec(floatCalculationTime*30*minuteValue)));


            }
        }
    }
}
