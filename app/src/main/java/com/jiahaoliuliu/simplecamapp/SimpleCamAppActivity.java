package com.jiahaoliuliu.simplecamapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


/***
 * Sample class with the implementation provided by the developer portal of Android:
 * http://developer.android.com/training/camera/photobasics.html
 */
public class SimpleCamAppActivity extends ActionBarActivity {

    private static final String TAG = "SimpleCamAppActivity";

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private Context mContext;

    // Layout
    private Button mStartCameraButton;
    private ImageView mPhotoImageView;

    // Other data
    private String mCurrentPhotoPath;
    private boolean mExternalStorageAvailable;
    private boolean mExternalStorageWriteable;

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
                    try {
                        // Create the File where the photo should go
                        File photoFile = createImageFile();
                        if (photoFile != null) {
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                        }
                    } catch (IOException ex) {
                        // Error occurred while creating the file
                        Log.e(TAG, "Error while creating a new file in the external public storage", ex);
                    }

                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Show the image if it is possible
            if (data != null) {
                Bundle extras = data.getExtras();
                Bitmap imageBitMap = (Bitmap) extras.get("data");
                mPhotoImageView.setImageBitmap(imageBitMap);
                mPhotoImageView.setVisibility(View.VISIBLE);
            }

            // Add the image to the gallery
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File file = new File(mCurrentPhotoPath);
            Uri contentUri = Uri.fromFile(file);
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);
        }
    }

    private File createImageFile() throws IOException {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mExternalStorageAvailable = mExternalStorageWriteable = true;
            Log.v(TAG, "The external storage is available and writable");
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
            Log.v(TAG, "The external storage is available but not writable");
        } else {
            mExternalStorageAvailable = mExternalStorageWriteable = false;
            Log.v(TAG, "The external storage is not available nor writable");
        }

        if (!mExternalStorageAvailable || !mExternalStorageWriteable) {
            return null;
        }

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

        // Make sure that the external storage exists
        storageDir.mkdir();
        File imagetmp = new File(storageDir, imageFileName+".jpg");

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + imagetmp.getAbsolutePath();
        return imagetmp;
    }
}
