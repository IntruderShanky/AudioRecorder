package com.islabs.recorderdialog;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

public class RecorderDialog extends DialogFragment implements View.OnClickListener {
    private static final String TAG = "Recorder Dialog";
    private String tempFile;
    private FloatingActionButton recordButton;
    private LinearLayout audioRecordLayout;
    private View audioPlayLayout;
    private int currentTime = 0;
    private MediaRecorder recorder = null;
    private MediaPlayer player = null;
    private TextView timeElapsed, duration;
    private ImageView playButton;
    private SeekBar seekBar;
    private Button save;
    private String destination;
    private OnRecordListener onRecordListener;

    private Handler timeHandler = new Handler();
    private Runnable timeRunnable = new Runnable() {
        @Override
        public void run() {
            currentTime += 1;
            int min = currentTime / 60;
            int sec = currentTime % 60;
            timeElapsed.setText(String.format(Locale.getDefault(), "%02d:%02d", min, sec));
            timeHandler.postDelayed(this, 1000);
        }
    };

    private Handler audioHandler = new Handler();
    private Runnable audioRunnable = new Runnable() {
        @Override
        public void run() {
            int currentPosition = player.getCurrentPosition();
            seekBar.setProgress(currentPosition);
            currentPosition /= 1000;
            int min = currentPosition / 60;
            int sec = currentPosition % 60;
            duration.setText(String.format(Locale.getDefault(), "%02d:%02d", min, sec));
            audioHandler.postDelayed(audioRunnable, 1000);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null)
            throw new RuntimeException("Use getInstance method to create instance of RecorderDialog");
        destination = getArguments().getString("destination");
    }

    /**
     * Automatically copy recorded file to destination
     */
    public static RecorderDialog getInstance(String destination) {
        RecorderDialog dialog = new RecorderDialog();
        Bundle bundle = new Bundle();
        bundle.putString("destination", destination);
        dialog.setArguments(bundle);
        return dialog;
    }

    /**
     * Will return recorded file to {@link OnRecordListener} callback if not null
     */
    public static RecorderDialog getInstance() {
        return getInstance("");
    }

    /**
     * Don't forget to delete returned file after your operation
     *
     * @param onRecordListener listen for events
     */
    public void setOnRecordListener(OnRecordListener onRecordListener) {
        this.onRecordListener = onRecordListener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        setCancelable(false);

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.record_audio_dialog, container, false);
        recordButton = view.findViewById(R.id.start_record);
        ImageView blinkMic = view.findViewById(R.id.blip_mic);
        audioRecordLayout = view.findViewById(R.id.audio_record_layout);
        audioPlayLayout = view.findViewById(R.id.audio_play_layout);
        timeElapsed = view.findViewById(R.id.time_elapsed);
        ImageView close = view.findViewById(R.id.close);
        duration = view.findViewById(R.id.duration);
        seekBar = view.findViewById(R.id.progress_bar);
        playButton = view.findViewById(R.id.play_button);
        save = view.findViewById(R.id.save_audio);

        tempFile = getActivity().getCacheDir().getPath() + "/temp.mp3";
        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.blink);

        blinkMic.startAnimation(animation);

        recordButton.setOnClickListener(this);
        close.setOnClickListener(this);
        playButton.setOnClickListener(this);
        save.setOnClickListener(this);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int currentPositiion, boolean b) {
                if (player != null && b) {
                    player.seekTo(currentPositiion);
                    int p = currentPositiion / 1000;
                    int min = p / 60;
                    int sec = p % 60;
                    duration.setText(String.format(Locale.getDefault(), "%02d:%02d", min, sec));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        return view;
    }

    private void startRecording() {
        save.setEnabled(false);
        recordButton.setBackgroundTintList(ColorStateList.valueOf(getThemeColor(R.attr.colorAccent)));
        timeElapsed.setText(String.format(Locale.getDefault(), "%02d:%02d", 0, 0));
        currentTime = 0;
        audioPlayLayout.setVisibility(View.GONE);
        audioRecordLayout.setVisibility(View.VISIBLE);
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        recorder.setOutputFile(tempFile);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e("Error ", "prepare() failed");
        }
        recorder.start();
        timeHandler.postDelayed(timeRunnable, 1000);
    }

    private void stopRecording() {
        try {
            save.setEnabled(true);
            recordButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#c2c2c2")));
            audioPlayLayout.setVisibility(View.VISIBLE);
            audioRecordLayout.setVisibility(View.GONE);
            timeHandler.removeCallbacks(timeRunnable);
            recorder.stop();
            recorder.release();
            recorder = null;
            seekBar.setProgress(0);
        } catch (Exception e) {
            e.printStackTrace();
            if (onRecordListener != null)
                onRecordListener.onError(e);
        }
    }

    private void startPlaying() {
        playButton.setImageResource(R.drawable.ic_pause);
        player = new MediaPlayer();
        try {
            player.setDataSource(tempFile);
            player.prepare();
            final int time = player.getDuration();
            seekBar.setMax(time);
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    stopPlaying();
                    seekBar.setProgress(time);
                }
            });
            seekBar.setProgress(0);
            player.start();
            audioHandler.post(audioRunnable);
        } catch (IOException e) {
            Log.e("Error  s", "prepare() failed");
        }
    }

    private void stopPlaying() {
        try {
            audioHandler.removeCallbacks(audioRunnable);
            playButton.setImageResource(R.drawable.ic_play);
            player.release();
            player = null;
        } catch (Exception e) {
            e.printStackTrace();
            if (onRecordListener != null) {
                onRecordListener.onError(e);
            }
        }
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.close) {
            if (recorder != null)
                stopRecording();
            if (player != null)
                stopPlaying();
            deleteTempFile();
            dismiss();

        } else if (i == R.id.play_button) {
            if (player != null && player.isPlaying()) stopPlaying();
            else startPlaying();

        } else if (i == R.id.start_record) {
            if (recorder != null) stopRecording();
            else startRecording();

        } else if (i == R.id.save_audio) {
            copyFileToDestination();
            if (onRecordListener != null) {
                onRecordListener.onPositiveButtonClick();
            }

        }
    }

    private void copyFileToDestination() {
        if (!destination.isEmpty())
            try {
                File sourceLocation = new File(tempFile);
                File targetLocation = new File(destination);
                InputStream in = new FileInputStream(sourceLocation);
                OutputStream out = new FileOutputStream(targetLocation);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
                if (onRecordListener != null) {
                    onRecordListener.onError(e);
                }
            }
        if (onRecordListener != null)
            onRecordListener.onRecorded(new File(tempFile));
        else deleteTempFile();
        dismiss();
    }

    /**
     * Delete temp files created by Recorder Dialog
     */
    public void deleteTempFile() {
        File file = new File(tempFile);
        if (file.exists())
            if (!file.delete())
                Log.w(TAG, "Unable to delete temp file");
    }

    @ColorInt
    public int getThemeColor(@AttrRes final int attributeColor) {
        final TypedValue value = new TypedValue();
        getActivity().getTheme().resolveAttribute(attributeColor, value, true);
        return value.data;
    }

    public void setDialogButtonText(String text) {
        save.setText(text);
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            this.getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onRecordListener != null) {
            onRecordListener.onDialogDismiss();
        }
    }
}
