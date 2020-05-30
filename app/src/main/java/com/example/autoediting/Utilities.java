package com.example.autoediting;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class Utilities {

    public static final String SHARED_SIZE_WIDTH = "size_width";
    public static final String SHARED_SIZE_HEIGHT = "size_height";
    public static final String SHARED_FRAME_DIFFERENCE = "difference";
    public static final String SHARED_SAMPLE_DURATION = "duration";
    public static final String VIDEO_FOR_SERVICE = "video_service";
    public static final String SHARED_ONE_CUT_DURATION = "one_cut";


    public static Bitmap getThumbnail(Uri image, Context context) throws  IOException {
        InputStream inputStream =  context.getContentResolver().openInputStream(image);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, onlyBoundsOptions);
        inputStream.close();

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;
        inputStream = context.getContentResolver().openInputStream(image);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, bitmapOptions);
        inputStream.close();

        return bitmap;
    }
}
