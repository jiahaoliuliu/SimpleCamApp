package com.jiahaoliuliu.simplecamapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dropbox.sync.android.DbxAccountManager;


/***
 * Sample class with the implementation provided by the developer portal of Android:
 * http://developer.android.com/training/camera/photobasics.html
 */
public class SimpleCamAppActivity extends ActionBarActivity {

    private static final String TAG = "SimpleCamAppActivity";
    private static final int REQUEST_LINK_TO_DBX = 1000;

    private static final String APP_KEY = "kwit9ifofwpmm2s";
    private static final String APP_SECRET = "raff2qnop6lssta";

    private Context mContext;

    // Dropbox
    private DbxAccountManager mDbxAccountManager;

    // Layout
    private Button mSyncDropboxAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Lock the screen orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_simple_cam_app);

        mContext = this;
        mDbxAccountManager = DbxAccountManager.getInstance(getApplicationContext(), APP_KEY, APP_SECRET);

        // Link the layout
        mSyncDropboxAccountButton = (Button)findViewById(R.id.syncDropboxAccountButton);
        mSyncDropboxAccountButton.setOnClickListener(onClickListener);

    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.syncDropboxAccountButton:
                    mDbxAccountManager.startLink((Activity) SimpleCamAppActivity.this, REQUEST_LINK_TO_DBX);
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        // Check if the app was already linked. If so, go to the sync activity
        if (mDbxAccountManager.hasLinkedAccount()) {
            Intent startTakePhotoActivityIntent = new Intent(SimpleCamAppActivity.this, TakePhotoActivity.class);
            startActivity(startTakePhotoActivityIntent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LINK_TO_DBX) {
            if (resultCode == Activity.RESULT_OK && mDbxAccountManager.hasLinkedAccount()) {
                Log.v(TAG, "DropBox account linked correctly");
                Intent startTakePhotoActivityIntent = new Intent(SimpleCamAppActivity.this, TakePhotoActivity.class);
                startActivity(startTakePhotoActivityIntent);
            } else {
                Log.w(TAG, "Error linking DropBox Account. The result is " + resultCode);
                Toast.makeText(mContext,
                        getResources().getString(R.string.error_message_account_not_linked),
                        Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
