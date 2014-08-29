package com.jiahaoliuliu.simplecamapp;

import android.content.ContentResolver;
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

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


/***
 * Sample class with the implementation provided by the developer portal of Android:
 * http://developer.android.com/training/camera/photobasics.html
 */
public class TakePhotoActivity extends ActionBarActivity {

    private static final String TAG = "SimpleCamAppActivity";

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private Context mContext;

    // Dropbox
    private DbxAccountManager mDbxAccountManager;
    private DbxFileSystem mDbxFileSystem;

    // Layout
    private Button mStartCameraButton;
    private ImageView mPhotoTakenImageView;

    // The photo file
    private File mPhotoFile;

    // Other data
    private String mCurrentPhotoPath;
    private String mImageFileName;

    private boolean mExternalStorageAvailable;
    private boolean mExternalStorageWriteable;

    private Uri initialUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Lock the screen orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.take_photo_activity_layout);

        mContext = this;
        mDbxAccountManager = DbxAccountManager.getInstance(getApplicationContext(), SimpleCamAppActivity.APP_KEY, SimpleCamAppActivity.APP_SECRET);

        mStartCameraButton = (Button)findViewById(R.id.startCameraButton);
        mStartCameraButton.setOnClickListener(onClickListener);

        mPhotoTakenImageView = (ImageView)findViewById(R.id.photoTakenImageView);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.startCameraButton:
                    // If the device does not have camera, do not anything
                    if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                        Toast.makeText(mContext, R.string.error_message_no_camera_detected, Toast.LENGTH_LONG).show();
                        break;
                    }

                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    // If there is any component module which can handle this intent
                    // This will prevent the crash when the app is calling the startActivityForResult
                    // when no app can handle it
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        try {
                            // Create the File where the photo should go
                            mPhotoFile = createImageFile();
                            if (mPhotoFile != null) {
                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPhotoFile));
                                initialUri = Uri.fromFile(mPhotoFile);
                                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                            }
                        } catch (IOException ex) {
                            // Error occurred while creating the file
                            Log.e(TAG, "Error while creating a new file in the external public storage", ex);
                        }
                    }
                break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        // Sync the view
        if (mDbxAccountManager.hasLinkedAccount()) {
            try {
                mDbxFileSystem = DbxFileSystem.forAccount(mDbxAccountManager.getLinkedAccount());
            } catch (DbxException.Unauthorized unauthorized) {
                Log.e(TAG, "Error creating the DbxFileSystem ", unauthorized);
            }
        } else {
            Log.e(TAG, "The app must be linked with one Dropbox account");
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            setImageView(initialUri);
            uploadeImageToDropbox();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
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
        mImageFileName = "JPEG_" + timeStamp + "_" + ".jpg";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

        // Make sure that the external storage exists
        File tmpImageFile = new File(storageDir, mImageFileName);

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = tmpImageFile.getAbsolutePath();
        return tmpImageFile;
    }

    // Several options to set the image
    // Source: http://stackoverflow.com/questions/21984570/file-not-exist-android-image-taken-by-camera
    private void setImageView(Uri imageFileUri) {
        Log.v(TAG, "Initial uri " + imageFileUri.toString());

        // Option 1
        //mPhotoTakenImageView.setImageURI(imageFileUri);

        // Option 2
        getContentResolver().notifyChange(imageFileUri, null);
        ContentResolver cr = getContentResolver();

        try {
            // Set the image from the camera to the imageview
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(cr, imageFileUri);
            mPhotoTakenImageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            Log.e(TAG, "Error setting the image view", e);
        }
    }

    private void uploadeImageToDropbox() {
        // Add the image to the gallery
        try {
            DbxFile dropboxFile = mDbxFileSystem.create(new DbxPath(mImageFileName));
            try {
                dropboxFile.writeFromExistingFile(mPhotoFile, false);
            } catch (IOException e) {
                Log.e(TAG, "Error creating the dropbox file", e);
            } finally {
                dropboxFile.close();
            }
        } catch (DbxException e) {
            Log.e(TAG, "Error creating the dropbox file ", e);
        }
    }
}