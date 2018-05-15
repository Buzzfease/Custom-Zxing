package com.chuangtu.theworldishere.zxing.decoding;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.chuangtu.theworldishere.R;
import com.chuangtu.theworldishere.zxing.activity.CaptureActivity;
import com.chuangtu.theworldishere.zxing.camera.CameraManager;
import com.chuangtu.theworldishere.zxing.view.ViewfinderResultPointCallback;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;


import java.util.Vector;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;

/**
 * Created by Buzz on 2018/1/17.
 */

public final class CaptureActivityHandler extends Handler {
    private static final String TAG = CaptureActivityHandler.class.getSimpleName();
    private final CaptureActivity activity;
    private final DecodeThread decodeThread;
    private State state;

    public CaptureActivityHandler(CaptureActivity activity, Vector<BarcodeFormat> decodeFormats, String characterSet) {
        this.activity = activity;
        this.decodeThread = new DecodeThread(activity, decodeFormats, characterSet, new ViewfinderResultPointCallback(activity.getViewfinderView()));
        this.decodeThread.start();
        this.state = State.SUCCESS;
        CameraManager.get().startPreview();
        this.restartPreviewAndDecode();
    }

    public void handleMessage(Message message) {
        if(message.what == R.id.auto_focus) {
            if(this.state == State.PREVIEW) {
                CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
            }
        } else if(message.what == R.id.restart_preview) {
            Log.d(TAG, "Got restart preview message");
            this.restartPreviewAndDecode();
        } else if(message.what == R.id.decode_succeeded) {
            Log.d(TAG, "Got decode succeeded message");
            this.state = State.SUCCESS;
            Bundle bundle = message.getData();
            Bitmap barcode = bundle == null?null:(Bitmap)bundle.getParcelable("barcode_bitmap");
            this.activity.handleDecode((Result)message.obj, barcode);
        } else if(message.what == R.id.decode_failed) {
            this.state = State.PREVIEW;
            CameraManager.get().requestPreviewFrame(this.decodeThread.getHandler(), R.id.decode);
        } else if(message.what == R.id.return_scan_result) {
            Log.d(TAG, "Got return scan result message");
            this.activity.setResult(-1, (Intent)message.obj);
            this.activity.finish();
        } else if(message.what == R.id.launch_product_query) {
            Log.d(TAG, "Got product query message");
            String url = (String)message.obj;
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url));
            intent.addFlags(FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            this.activity.startActivity(intent);
        }

    }

    public void quitSynchronously() {
        this.state = State.DONE;
        CameraManager.get().stopPreview();
        Message quit = Message.obtain(this.decodeThread.getHandler(), R.id.quit);
        quit.sendToTarget();

        try {
            this.decodeThread.join();
        } catch (InterruptedException var3) {

        }

        this.removeMessages(R.id.decode_succeeded);
        this.removeMessages(R.id.decode_failed);
    }

    private void restartPreviewAndDecode() {
        if(this.state == State.SUCCESS) {
            this.state = State.PREVIEW;
            CameraManager.get().requestPreviewFrame(this.decodeThread.getHandler(), R.id.decode);
            CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
            this.activity.drawViewfinder();
        }

    }

    private static enum State {
        PREVIEW,
        SUCCESS,
        DONE;

        private State() {
        }
    }
}
