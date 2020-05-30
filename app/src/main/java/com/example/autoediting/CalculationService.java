package com.example.autoediting;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;


import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;


public class CalculationService extends IntentService {


    private static final String CHANNEL_ID = "channel";
    private static final String TAG = "Execute : ";
    private static final String FRAME_TAG = "Frame_Number";
    private static final String SECOND_FRAME_TAG = "Frame_Number";
    private ArrayList<Integer> mFrames;
    private static final int NOTIFICATION_ID = 1;
    private File finalDestination;
    private FFmpeg ffmpeg;
    private final Object syncObject = new Object();
    private ArrayList<Float> mTimeToCut;
    private float difference;
    private ArrayList<Integer> mIndexesToIgnore;
    private NotificationCompat.Builder progressNotification;
    private MediaMetadataRetriever mRetriever = new MediaMetadataRetriever();


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public CalculationService(String name) {
        super(name);
    }

    public CalculationService() {
        super("MyCalculations");
    }



    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        initialize(this);
        createNotificationChannel();
        final NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);



        mIndexesToIgnore = new ArrayList<>();
        mFrames = new ArrayList<>();
        mTimeToCut = new ArrayList<>();
        ArrayList<String> mCutDestinations = new ArrayList<>();

        //used only for synchronizing



        /*movieUri - Uri of the movie to edit
        frameWidth, frameHeight - dimensions of each frame
        difference - difference between 2 frames for program to detect and make a cut
        cutMinDuration - minimal durations of 1 clip after the cut
        duration - duration of whole movie to work on
        */

        Uri movieUri = (Uri) intent.getExtras().get(Utilities.VIDEO_FOR_SERVICE);
        int frameWidth = (int) intent.getExtras().get(Utilities.SHARED_SIZE_WIDTH);
        int frameHeight = (int) intent.getExtras().get(Utilities.SHARED_SIZE_HEIGHT);
        difference = (float) intent.getExtras().get(Utilities.SHARED_FRAME_DIFFERENCE);
        int cutMinDuration = (int) intent.getExtras().get(Utilities.SHARED_ONE_CUT_DURATION);
        mRetriever.setDataSource(this, movieUri);
        int duration = Integer.parseInt(mRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

        //Starting frame number and first value
        int frameNumber = (15 * cutMinDuration);
        float valueOfPreviousFrame = 0;

        //Always cut at the begining
        int lastCutted = 1;

        progressNotification = new NotificationCompat.Builder(this, CHANNEL_ID);
        progressNotification.setContentTitle("Editing video")
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentText("Searching for frames to cut video")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setSmallIcon(R.drawable.ic_movie)
                .setProgress(duration, 0 , false);

        startForeground(NOTIFICATION_ID, progressNotification.build());
        Log.d("Starting for Width : ",frameWidth + "x" + frameHeight);

        //All the calculations made to extract value of 1 frame
        for(int i = 1 + (66*15* cutMinDuration); i<duration-66; i = i + 66)
        {
            float sum=0;
            float size=0;

            if(frameNumber ==114)
            {
                Log.d("Klatka 114", String.valueOf(i));
            }
            Bitmap mCurrentBitmap = mRetriever.getFrameAtTime(i * 1000, MediaMetadataRetriever.OPTION_CLOSEST);
            mCurrentBitmap = Bitmap.createScaledBitmap(mCurrentBitmap, frameWidth, frameHeight, false);

            if(frameNumber >= lastCutted + 15* cutMinDuration - 1) {
                for (int x = 0; x < frameWidth; x++) {
                    for (int y = 0; y < frameHeight; y++) {
                        int pixel = mCurrentBitmap.getPixel(x, y);
                        float redValue = Color.red(pixel) / 255;
                        float blueValue = Color.blue(pixel) / 255;
                        float greenValue = Color.green(pixel) / 255;
                        float pixelValue = (redValue + blueValue + greenValue) / 3;
                        sum += pixelValue;
                        size++;
                    }
                }
            }

            Log.d(SECOND_FRAME_TAG, String.valueOf(frameNumber));
            if(frameNumber >= lastCutted + 15* cutMinDuration) {
                if (shouldCut(sum / size, valueOfPreviousFrame)) {
                mFrames.add(frameNumber*2);
                    Log.d(FRAME_TAG, "We should cut at: " + frameNumber);
                    lastCutted = frameNumber;
                    //Can skip cutMinDuration seconds of calculations because of cut
                    i = i+(66*15* cutMinDuration);
                    //Number of frames too
                    frameNumber = frameNumber + (15* cutMinDuration)-2;
                }
            }
            valueOfPreviousFrame = sum/size;
            Log.d(FRAME_TAG, String.valueOf(valueOfPreviousFrame));
            frameNumber++;

            progressNotification.setProgress(duration, i, false);
            notificationManagerCompat.notify(NOTIFICATION_ID, progressNotification.build());
        }


        FramesToTime(mFrames);
        String destPath = "/storage/emulated/0/DCIM/AutoEdith/";
        File externalStoragePublicDirectory = new File(destPath);
        int i = 0;
        progressNotification.setContentTitle("Cutting video")
                .setContentText("Frames found : " + mFrames.toString() + ". Cutting video...");
        for(Iterator<Float> it = mTimeToCut.iterator(); it.hasNext();)
        {
            Float time = it.next();
            int currentCutDuration = cutMinDuration;
            int size = mTimeToCut.size();
            int index = mTimeToCut.indexOf(time);


            if(!mIndexesToIgnore.contains(index)) {
                if (index < size) {
                    int initial = 1;
                    while (index+initial < size && mTimeToCut.get(index + initial) <= time + currentCutDuration + 0.1) {
                        Log.d("Current size and index : ", size + " " + index);
                        currentCutDuration = currentCutDuration + cutMinDuration;
                        Log.d("Item to remove : ", mTimeToCut.get(index + 1) + "");
                        mIndexesToIgnore.add(index + initial);
                        initial++;
                    }
                }

                float cutTo = time + currentCutDuration;
                Log.d("Should cut from to : ", time.toString() + " : " + cutTo);

                if (!externalStoragePublicDirectory.exists()) {
                    externalStoragePublicDirectory.mkdir();
                }


                String filePrefix = "video_cut";
                String fileExtension = ".mp4";
                File destination = new File(externalStoragePublicDirectory, filePrefix + fileExtension);
                if (destination.exists()) {
                    i++;
                    destination = new File(externalStoragePublicDirectory, filePrefix + i + fileExtension);

                }
                mCutDestinations.add(destination.getAbsolutePath());
                String original_path = getRealPathFromUri(this, movieUri);
                Log.d("Destination and path : ", destination + " : " + original_path);
                String[] command = {"-ss", "" + time, "-y", "-i", original_path, "-t", "" + currentCutDuration, "-async", "" + 1, "-vcodec", "mpeg4","-b:v", "2097152", "-b:a", "48000", "-ac", "2", "-ar", "22050", "-strict", "-2", destination.getAbsolutePath()};
//                String[] command = {"-y", "-i", original_path, "-ss", ""+time, "-t", "" + currentCutDuration, "-c", "copy", destination.getAbsolutePath()};

                synchronized (syncObject) {
                    try {
                        executeCmd(command, index);
                        syncObject.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }


        finalDestination = new File(externalStoragePublicDirectory, "autoEdith_00" + ".mp4");
        int numberOfFile=1;
        while (finalDestination.exists())
        {
            finalDestination = new File(externalStoragePublicDirectory, "autoEdith_0" +numberOfFile+ ".mp4");
            numberOfFile++;
        }
        String list = generateList(GetStringArray(mCutDestinations));
        String[] commandConcat = {"-f", "concat", "-safe", ""+ 0,"-i", list, "-c", "copy", finalDestination.getAbsolutePath()};


        synchronized (syncObject)
        {
            try {
                executeCmd(commandConcat,-1);
                syncObject.wait();
            }catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        for(String cutFileDestination : mCutDestinations)
        {
            File cutFile = new File(cutFileDestination);
            cutFile.delete();
        }
        viewVideoNotification(notificationManagerCompat);
        stopForeground(true);
    }

    /**Creating a String Array from normal ArrayList of Strings
     *
     * @param arrayList Array List of String
     * @return should return String Array created from items of Array List of Strings
     */
    public static String[] GetStringArray(ArrayList<String> arrayList)
    {
        String[] str = new String[arrayList.size()];

        for(int j = 0; j< arrayList.size(); j++)
        {
            str[j] = arrayList.get(j);
        }

        return str;
    }

    /**Changing frames found to cut to time
     *
     * @param mFrames
     */
    private void FramesToTime(ArrayList<Integer> mFrames)
    {
        for(Integer frame : mFrames)
        {
            float time = (float)frame/ 30;
            mTimeToCut.add(time);
        }
        Log.d("FramesToTime", mTimeToCut.toString());
    }

    //Generate temp list.txt file for FFmpeg concat to know which clips to concat
    private static String generateList(String[] inputs) {
        File list;
        Writer writer = null;
        try {
            list = File.createTempFile("ffmpeg-list", ".txt");
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(list)));
            for (String input: inputs) {
                writer.write("file '" + input + "'\n");
                Log.d(TAG, "Writing to list file: file '" + input + "'");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "/";
        } finally {
            try {
                if (writer != null)
                    writer.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        Log.d(TAG, "Wrote list file to " + list.getAbsolutePath());
        return list.getAbsolutePath();
    }

    /**Getting real path of file based on its uri
     *
     * @param context Context needed to get ContentResolver
     * @param contentUri Uri for the file we want get Path from
     * @return Should return the real path from device
     */
    private String getRealPathFromUri(Context context, Uri contentUri)
    {
        Cursor cursor = null;

        try
        {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

            cursor.moveToFirst();
            return cursor.getString(column_index);
        }catch ( Exception e)
        {
            e.printStackTrace();
            return "";
        }finally {
            if(cursor != null)
            {
                cursor.close();
            }
        }

    }

    /**Create a final notification for edited movie preview
     *
     * @param notificationManagerCompat Notification Manager used for displaying notification
     */
    private void viewVideoNotification(NotificationManagerCompat notificationManagerCompat) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setContentTitle("Editing complete!")
                .setContentText("All editing is done. " + "Frames found: " + mFrames.toString())
                .setSmallIcon(R.drawable.ic_movie)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent showResult = new Intent(Intent.ACTION_VIEW);
        showResult.setDataAndType(Uri.parse(finalDestination.getAbsolutePath()), "video/*");
        showResult.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0,
                showResult,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        notificationManagerCompat.notify(2, builder.build());
    }

    /**Execute various FFmpeg commands
     *
     * @param command Command to execute
     */
    private void executeCmd(final String[] command, final int index)
    {
        try {

            ffmpeg.execute(command, new ExecuteBinaryResponseHandler()
            {
                @Override
                public void onSuccess(String message) {
                    super.onSuccess(message);
                    Log.d("FFmpeg initialize : ", "Finished successful");
                    if(index>=0)
                    {
                        progressNotification.setProgress(mTimeToCut.size(),index+1,false);
                    }
                    synchronized (syncObject)
                    {
                        syncObject.notify();
                    }
                }

                @Override
                public void onProgress(String message) {
                    super.onProgress(message);
                }

                @Override
                public void onFailure(String message) {
                    super.onFailure(message);
                    Log.d(TAG, "FAILED with output : " + message);
                }

                @Override
                public void onStart() {
                    super.onStart();
                    Log.d(TAG, "Started command : ffmpeg " + command);
                }
            });
        }catch (FFmpegCommandAlreadyRunningException e)
        {
            e.printStackTrace();
        }

    }

    /**Method that determinate if the program should cut the video between these 2 frames
     *
     * @param valueOne first value to compare
     * @param valueTwo second value to compare
     * @return should return True if the difference is greater than difference parameter, False otherwise
     */
    private boolean shouldCut(float valueOne, float valueTwo)
    {
        float currentDifference;
        if(valueOne < valueTwo)
        {
            currentDifference = 1 - valueOne/valueTwo;
            if(currentDifference >= difference)
            {
                return true;
            } else return false;
        }else
        {
            currentDifference = 1 - valueTwo/valueOne;
            if(currentDifference >= difference)
            {
                return true;
            } else return false;
        }
    }

    /**Creating NotificationChannel for Android Version > 0
     *
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = "Editing Channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**FFmpeg initialization
     *
     * @param context context used for initialization of FFmpeg
     */
    public void initialize(Context context)
    {
        ffmpeg = FFmpeg.getInstance(context.getApplicationContext());
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler()
            {
                @Override
                public void onSuccess() {
                    super.onSuccess();
                    Log.d("FFmpeg initialize : ", "Initialized successful");
                }
            });
        } catch (FFmpegNotSupportedException e)
        {
            Log.d("FFmpeg initialize : ", "Initialized failed");

        }
    }
}
