package com.chuangtu.theworldishere.zxing.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.chuangtu.theworldishere.R;
import com.chuangtu.theworldishere.zxing.camera.CameraManager;
import com.chuangtu.theworldishere.zxing.decoding.CaptureActivityHandler;
import com.chuangtu.theworldishere.zxing.decoding.InactivityTimer;
import com.chuangtu.theworldishere.zxing.view.ViewfinderView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;


import java.io.IOException;
import java.util.Vector;

/**
 * Created by Buzz on 2018/1/17.
 */

public class CaptureActivity extends Activity implements SurfaceHolder.Callback {
    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.1F;
    private boolean vibrate;
    private Button cancelScanButton;
    private static final long VIBRATE_DURATION = 200L;
    private final MediaPlayer.OnCompletionListener beepListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };

    public CaptureActivity() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.camera);
        CameraManager.init(this.getApplication());
        this.viewfinderView = (ViewfinderView)this.findViewById(R.id.viewfinder_view);
        this.cancelScanButton = (Button)this.findViewById(R.id.btn_cancel_scan);
        this.hasSurface = false;
        this.inactivityTimer = new InactivityTimer(this);
    }

    protected void onResume() {
        super.onResume();
        SurfaceView surfaceView = (SurfaceView)this.findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if(this.hasSurface) {
            this.initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(3);
        }

        this.decodeFormats = null;
        this.characterSet = null;
        this.playBeep = true;
        AudioManager audioService = (AudioManager)this.getSystemService(AUDIO_SERVICE);
        if(audioService.getRingerMode() != 2) {
            this.playBeep = false;
        }

        this.initBeepSound();
        this.vibrate = true;
        this.cancelScanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }

    protected void onPause() {
        super.onPause();
        if(this.handler != null) {
            this.handler.quitSynchronously();
            this.handler = null;
        }

        CameraManager.get().closeDriver();
    }

    protected void onDestroy() {
        this.inactivityTimer.shutdown();
        super.onDestroy();
    }

    public void handleDecode(Result result, Bitmap barcode) {
        this.inactivityTimer.onActivity();
        this.playBeepSoundAndVibrate();
        String resultString = result.getText();
        if(resultString.equals("")) {
            Toast.makeText(this, "Scan failed!", Toast.LENGTH_SHORT).show();
        } else {
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("result", resultString);
            resultIntent.putExtras(bundle);
            this.setResult(-1, resultIntent);
        }

        this.finish();
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (IOException var3) {
            return;
        } catch (RuntimeException var4) {
            return;
        }

        if(this.handler == null) {
            this.handler = new CaptureActivityHandler(this, this.decodeFormats, this.characterSet);
        }

    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    public void surfaceCreated(SurfaceHolder holder) {
        if(!this.hasSurface) {
            this.hasSurface = true;
            this.initCamera(holder);
        }

    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        this.hasSurface = false;
    }

    public ViewfinderView getViewfinderView() {
        return this.viewfinderView;
    }

    public Handler getHandler() {
        return this.handler;
    }

    public void drawViewfinder() {
        this.viewfinderView.drawViewfinder();
    }

    private void initBeepSound() {
        if(this.playBeep && this.mediaPlayer == null) {
            this.setVolumeControlStream(3);
            this.mediaPlayer = new MediaPlayer();
            this.mediaPlayer.setAudioStreamType(3);
            this.mediaPlayer.setOnCompletionListener(this.beepListener);
            AssetFileDescriptor file = this.getResources().openRawResourceFd(R.raw.beep);

            try {
                this.mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
                file.close();
                this.mediaPlayer.setVolume(0.1F, 0.1F);
                this.mediaPlayer.prepare();
            } catch (IOException var3) {
                this.mediaPlayer = null;
            }
        }

    }

    private void playBeepSoundAndVibrate() {
        if(this.playBeep && this.mediaPlayer != null) {
            this.mediaPlayer.start();
        }

        if(this.vibrate) {
            Vibrator vibrator = (Vibrator)this.getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(200L);
        }

    }
}
