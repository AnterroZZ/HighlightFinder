package com.example.autoediting;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.autoediting.Preferences.SettingsActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StartingActivity extends AppCompatActivity {

    private static final int RESULT_VIDEO = 3;
    public static final String UPLOADED_VIDEO = "videoUploaded";
    private String comparePhotos;
    private String chooseGallery;
    private String cancelDialog;
    private ImageView backgroundOne;
    private ImageView backgroundTwo;
    private Toolbar mToolbar;
    public static final int RESULT_GALLERY = 2;
    public static final String UPLOADED_IMAGE = "uploaded";
    public static final String TAKEN_IMAGE = "taken";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }
        setContentView(R.layout.activity_starting);

        comparePhotos = getResources().getString(R.string.compare_two);
        chooseGallery = getResources().getString(R.string.choose_gallery);
        cancelDialog = getResources().getString(R.string.cancel);

        backgroundOne = findViewById(R.id.background_one);
        backgroundTwo = findViewById(R.id.background_two);

        setUpToolBar();
        setUpAnimator();
    }

    private void setUpAnimator() {
        ValueAnimator animator = ValueAnimator.ofFloat(0.0f, 1.0f);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(10000L);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (float) animation.getAnimatedValue();
                float width = backgroundOne.getWidth();
                float translationX = width * progress;
                backgroundOne.setTranslationX(translationX);
                backgroundTwo.setTranslationX(translationX-width);
            }
        });
        animator.start();
    }

    private void setUpToolBar() {
        mToolbar = findViewById(R.id.start_toolbar);
        mToolbar.inflateMenu(R.menu.start_menu);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if(id==R.id.action_settings)
                {
                    Intent settingsIntent = new Intent(StartingActivity.this, SettingsActivity.class);
                    startActivity(settingsIntent);
                    return true;
                }
                return false;
            }
        });
    }

    //TODO: To be done
    public void uploadMovie(View view) {
        Intent videoIntent = new Intent();
        videoIntent.setType("video/*");
        videoIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(videoIntent, "Select Video"), RESULT_VIDEO);
    }

    public void uploadImage(View view) {
        final String[] uploadOptions = {chooseGallery, comparePhotos, cancelDialog};
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.PreferenceDialogTheme
        );
        builder.setTitle("Add Photo!");
        builder.setItems(uploadOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               if(uploadOptions[which].equals(chooseGallery))
                {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(galleryIntent, RESULT_GALLERY);
                }else if (uploadOptions[which].equals(comparePhotos)){
                   Intent compareIntent = new Intent(StartingActivity.this, CompareActivity.class);
                   startActivity(compareIntent);
                }else if(uploadOptions[which].equals(cancelDialog))
                {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            if (requestCode == RESULT_GALLERY && data != null) {
                Intent editActivity = new Intent(this, PerformanceActivity.class);
                Uri selectedImage = data.getData();

                editActivity.putExtra(UPLOADED_IMAGE, selectedImage);
                startActivity(editActivity);

            }else if(requestCode == RESULT_VIDEO && data != null)
            {
                Intent videoActivity = new Intent(this, VideoActivity.class);
                Uri videoUri = data.getData();
                videoActivity.putExtra(UPLOADED_VIDEO, videoUri);
                startActivity(videoActivity);
            }
        }
    }
}
