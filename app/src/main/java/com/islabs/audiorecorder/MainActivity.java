package com.islabs.audiorecorder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.islabs.recorderdialog.OnRecordListener;
import com.islabs.recorderdialog.RecorderDialog;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private boolean permissionToRecordAccepted;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) finish();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
    }

    public void getAudio(View view) {
        RecorderDialog dialog = RecorderDialog.getInstance(getExternalCacheDir().getPath() + "/my_recorded_audio.mp3");
        dialog.show(getFragmentManager(), "Audio Recorder");

        dialog.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onRecorded(File file) {

            }

            @Override
            public void onError(Exception e) {

            }

            @Override
            public void onDialogDismiss() {

            }

            @Override
            public void onPositiveButtonClick() {

            }
        });
    }

}
