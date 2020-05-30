package com.example.autoediting;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;

public class CompareActivity extends AppCompatActivity {

    private ProgressBar mProgressBar1;
    private ProgressBar mProgressBar2;

    private ImageView mImage1;
    private ImageView mImage2;

    private TextView mValue1;
    private TextView mValue2;
    private TextView mDifference;
    private boolean isImage1;

    private int rescaledWidth;
    private int rescaledHeight;

    public static final int RESULT_GALLERY_1 = 1;
    public static final int RESULT_GALLERY_2 = 2;

    private float mCalculated1;
    private float mCalculated2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare);

        mProgressBar1 = findViewById(R.id.compare_progressbar_1);
        mProgressBar2 = findViewById(R.id.compare_progressbar_2);
        mImage1 = findViewById(R.id.image_1);
        mImage2 = findViewById(R.id.image_2);
        mValue1 = findViewById(R.id.compare_calculated_value_1);
        mValue2 = findViewById(R.id.compare_calculated_value_2);
        mDifference = findViewById(R.id.compare_difference_value);

        mProgressBar1.setVisibility(View.INVISIBLE);
        mProgressBar2.setVisibility(View.INVISIBLE);
        mValue1.setVisibility(View.INVISIBLE);
        mValue2.setVisibility(View.INVISIBLE);

        rescaledWidth = 1280;
        rescaledHeight = 720;

        mCalculated1 = 0;
        mCalculated2 = 0;
    }

    public void uploadImage1(View view) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, RESULT_GALLERY_1);
    }

    public void uploadImage2(View view) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, RESULT_GALLERY_2);
    }

    /**Getting results from intents starting activities
     *
     * @param requestCode Individual value for each startActivityForResult method
     * @param resultCode Value indicating if the result is a success
     * @param data Intent containing all data returned from activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK)
        {
            if(requestCode == RESULT_GALLERY_1 && data != null)
            {
                Uri selectedImage = data.getData();
                Bitmap imageBitmap = null;

                try {
                    imageBitmap = Utilities.getThumbnail(selectedImage, this);
                }catch (IOException e)
                {
                    e.printStackTrace();
                }

                mImage1.setImageBitmap(imageBitmap);
                mImage1.setClickable(false);
                isImage1 = true;

                new BitMapToSingleValue().execute(imageBitmap);

            } else if (requestCode == RESULT_GALLERY_2 && data != null) {
                Uri selectedImage = data.getData();
                Bitmap imageBitmap = null;

                try {
                    imageBitmap = Utilities.getThumbnail(selectedImage, this);
                }catch (IOException e)
                {
                    e.printStackTrace();
                }

                mImage2.setImageBitmap(imageBitmap);
                mImage2.setClickable(false);
                isImage1 = false;

                new BitMapToSingleValue().execute(imageBitmap);

            }
        }
    }



    private class BitMapToSingleValue extends AsyncTask <Bitmap, Integer, Float>
    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(isImage1)
            {
                mProgressBar1.setVisibility(View.VISIBLE);
                mValue1.setVisibility(View.INVISIBLE);
                mProgressBar1.setMax(100);
            }else
            {
                mProgressBar2.setVisibility(View.VISIBLE);
                mValue2.setVisibility(View.INVISIBLE);
                mProgressBar2.setMax(100);
            }
        }



        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            if(isImage1)
            {
                mProgressBar1.setProgress(values[0]);
            }else
            {
                mProgressBar2.setProgress(values[0]);
            }
        }

        @Override
        protected Float doInBackground(Bitmap... bitmaps) {
            float sum = 0;
            float size = 0;
            Bitmap rescaledForCalculations = Bitmap.createScaledBitmap(bitmaps[0], rescaledWidth,rescaledHeight, false);
            for(int x=0; x<rescaledWidth; x++)
            {
                for(int y=0; y<rescaledHeight; y++)
                {
                    int pixel = rescaledForCalculations.getPixel(x,y);
                    float redValue = Color.red(pixel) / 255;
                    float blueValue = Color.blue(pixel) / 255;
                    float greenValue = Color.green(pixel) / 255;
                    float pixelValue = (redValue + blueValue + greenValue) / 3;
                    sum += pixelValue;
                    size++;
                }
                publishProgress(x * 100 / rescaledWidth);
            }

            return sum/size;
        }

        @Override
        protected void onPostExecute(Float aFloat) {
            super.onPostExecute(aFloat);

            if(isImage1)
            {
                mProgressBar1.setVisibility(View.INVISIBLE);
                mValue1.setVisibility(View.VISIBLE);
                mValue1.setText(String.valueOf(aFloat));
                mCalculated1 = aFloat;

                showDifference();
            }else
            {
                mProgressBar2.setVisibility(View.INVISIBLE);
                mValue2.setVisibility(View.VISIBLE);
                mValue2.setText(String.valueOf(aFloat));
                mCalculated2 = aFloat;

                showDifference();
            }
        }

    }

    private void showDifference() {
        if(mCalculated1 != 0 && mCalculated2 != 0)
        {
            float difference;
           if(mCalculated1 > mCalculated2)
           {
               difference = 1 - mCalculated2 / mCalculated1;

           }else
           {
               difference = 1 - mCalculated1 / mCalculated2;
           }
            mDifference.setText(String.valueOf(difference));

        }
    }


}
