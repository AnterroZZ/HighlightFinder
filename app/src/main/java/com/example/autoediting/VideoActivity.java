package com.example.autoediting;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import java.util.ArrayList;

public class VideoActivity extends AppCompatActivity {

    private static final int MILLIS_MINUTE = 60000;

    private boolean isMuted;
    private TextView mEstimatedTime;
    private ProgressBar mProgressBar;

    private long startTime;
    private int duration;

    private SharedPreferences mSharedPreferences;
    private MediaMetadataRetriever mRetriever = new MediaMetadataRetriever();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video);

        boolean isStarted = false;
        TextView mVideoLength = findViewById(R.id.video_videoDurationValue);
        mEstimatedTime = findViewById(R.id.video_estimatedTimeValue);
        TextView mFrameSamplingSize = findViewById(R.id.video_frameSamplingSizeValue);
        TextView mDifferenceDetecion = findViewById(R.id.video_differenceDetectionValue);
        VideoView mVideo = findViewById(R.id.video_videoView);
        mProgressBar = findViewById(R.id.video_estimatedProgressBar);
        isMuted = true;

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Intent resultIntent = getIntent();

        if(resultIntent.getExtras().get(StartingActivity.UPLOADED_VIDEO) != null)
        {
            Uri videoUri = (Uri) resultIntent.getExtras().get(StartingActivity.UPLOADED_VIDEO);
            mVideo.setVideoURI(videoUri);
            mVideo.start();

            mRetriever.setDataSource(this, videoUri);

            duration = Integer.parseInt(mRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            int frameHeight = mSharedPreferences.getInt(Utilities.SHARED_SIZE_HEIGHT, 720);
            int frameWidth = mSharedPreferences.getInt(Utilities.SHARED_SIZE_WIDTH, 1280);
            float difference = mSharedPreferences.getFloat(Utilities.SHARED_FRAME_DIFFERENCE, (float) 0.1);
            int cutMinDuration = mSharedPreferences.getInt(Utilities.SHARED_ONE_CUT_DURATION, 1);

            if(duration > MILLIS_MINUTE)
            {
                int seconds = duration % MILLIS_MINUTE;
                seconds = seconds / 1000;
                int minutes = duration - seconds;
                minutes = minutes / MILLIS_MINUTE;
                mVideoLength.setText(minutes + " min " + seconds + " sec");
            }else {
                int seconds = duration / 1000;
                mVideoLength.setText(seconds + " sec");
            }



            mFrameSamplingSize.setText(String.format("%dx%d", frameWidth, frameHeight));
            mDifferenceDetecion.setText(String.valueOf(difference));
            mEstimatedTime.setText(String.valueOf(((int)duration/1000)*30*0.2));


            new CalculateEstimatedTime().execute(mRetriever.getFrameAtTime(1));

            Intent serviceIntent = new Intent(this, CalculationService.class);
            serviceIntent.putExtra(Utilities.SHARED_ONE_CUT_DURATION, cutMinDuration);
            serviceIntent.putExtra(Utilities.VIDEO_FOR_SERVICE, videoUri);
            serviceIntent.putExtra(Utilities.SHARED_SIZE_WIDTH, frameWidth);
            serviceIntent.putExtra(Utilities.SHARED_SIZE_HEIGHT, frameHeight);
            serviceIntent.putExtra(Utilities.SHARED_FRAME_DIFFERENCE, difference);
            startService(serviceIntent);


        }
    }

    private class CalculateEstimatedTime extends AsyncTask<Bitmap, Integer, Float>
    {
        @Override
        protected void onPreExecute() {
            startTime = System.currentTimeMillis();
            mProgressBar.setVisibility(View.VISIBLE);
            mEstimatedTime.setVisibility(View.INVISIBLE);
            mProgressBar.setMax(100);
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            mProgressBar.setProgress(values[0]);
        }

        @Override
        protected Float doInBackground(Bitmap... bitmaps) {
            float sum = 0;
            float size = 0;
            int width = mSharedPreferences.getInt(Utilities.SHARED_SIZE_WIDTH, 1280);
            int height =  mSharedPreferences.getInt(Utilities.SHARED_SIZE_HEIGHT, 720);
            Bitmap rescaled = Bitmap.createScaledBitmap(bitmaps[0], width, height,false);
            for(int x = 0; x< width; x++)
            {
                for(int y=0; y<height; y++)
                {
                    int pixel = rescaled.getPixel(x,y);
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
            super.onPostExecute(aFloat);

            long endTime = System.currentTimeMillis();
            double calculationTime = endTime - startTime;
            float calculationFloat = (float) calculationTime;
            float timeInSeconds = (float)(15*calculationTime/1000*duration/1000);
            Log.d("Time", String.valueOf(calculationTime/1000));
            Log.d("Time", String.valueOf(calculationFloat/1000));
            Log.d("Time", String.valueOf(duration));
            Log.d("Time", String.valueOf(timeInSeconds));
            if(timeInSeconds > 60)
            {
                int seconds = (int)(timeInSeconds % 60);
                int minutes = (int)timeInSeconds - seconds;
                minutes = minutes / 60;
                mEstimatedTime.setText("~" + minutes + " min " + seconds + " sec");
            }else {
                int seconds = (int)timeInSeconds;
                mEstimatedTime.setText("~" + seconds + " sec");
            }
            mProgressBar.setVisibility(View.INVISIBLE);
            mEstimatedTime.setVisibility(View.VISIBLE);
        }
    }

    public void muteUnmute(View view) {
        CustomVideoPlayer player = (CustomVideoPlayer) view;
        if(isMuted)
        {
            player.unmute();
            isMuted = false;
        }else {
            player.mute();
            isMuted = true;
        }
    }
}
