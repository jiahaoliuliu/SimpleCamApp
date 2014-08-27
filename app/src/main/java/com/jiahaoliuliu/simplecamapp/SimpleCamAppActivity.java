package com.jiahaoliuliu.simplecamapp;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class SimpleCamAppActivity extends ActionBarActivity {

    private Context context;
    private Button startCameraButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Lock the screen orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_simple_cam_app);

        context = this;

        startCameraButton = (Button)findViewById(R.id.startCameraButton);

        startCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If the device does not have camera, do not anything
                if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                    Toast.makeText(context, R.string.error_message_no_camera_detected, Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });
    }
}
