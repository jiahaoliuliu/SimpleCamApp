package com.jiahaoliuliu.simplecamapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


public class SimpleCamAppActivity extends ActionBarActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private Context mContext;

    // Layout
    private Button mStartCameraButton;
    private ImageView mPhotoImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Lock the screen orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_simple_cam_app);

        mContext = this;

        mStartCameraButton = (Button)findViewById(R.id.startCameraButton);
        mPhotoImageView = (ImageView)findViewById(R.id.photoImageView);

        mStartCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If the device does not have camera, do not anything
                if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                    Toast.makeText(mContext, R.string.error_message_no_camera_detected, Toast.LENGTH_LONG).show();
                    return;
                }

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // If there is any component module which can handle this intent
                // This will prevent the crash when the app is calling the startActivityForResult
                // when no app can handle it
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitMap = (Bitmap) extras.get("data");
            mPhotoImageView.setImageBitmap(imageBitMap);
            mPhotoImageView.setVisibility(View.VISIBLE);
        }
    }
}
