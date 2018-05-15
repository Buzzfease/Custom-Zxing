package com.chuangtu.theworldishere.zxing.decoding;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.chuangtu.theworldishere.R;
import com.chuangtu.theworldishere.zxing.activity.CaptureActivity;
import com.chuangtu.theworldishere.zxing.camera.CameraManager;
import com.chuangtu.theworldishere.zxing.camera.PlanarYUVLuminanceSource;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;


import java.util.Hashtable;

/**
 * Created by Buzz on 2018/1/17.
 */

final class DecodeHandler extends Handler {
    private static final String TAG = DecodeHandler.class.getSimpleName();
    private final CaptureActivity activity;
    private final MultiFormatReader multiFormatReader = new MultiFormatReader();

    DecodeHandler(CaptureActivity activity, Hashtable<DecodeHintType, Object> hints) {
        this.multiFormatReader.setHints(hints);
        this.activity = activity;
    }

    public void handleMessage(Message message) {
        if(message.what == R.id.decode) {
            this.decode((byte[])((byte[])message.obj), message.arg1, message.arg2);
        } else if(message.what == R.id.quit) {
            Looper.myLooper().quit();
        }

    }

    private void decode(byte[] data, int width, int height) {
        long start = System.currentTimeMillis();
        Result rawResult = null;
        byte[] rotatedData = new byte[data.length];

        for(int y = 0; y < height; ++y) {
            for(int x = 0; x < width; ++x) {
                rotatedData[x * height + height - y - 1] = data[x + y * width];
            }
        }

        PlanarYUVLuminanceSource source = CameraManager.get().buildLuminanceSource(rotatedData, height, width);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        try {
            rawResult = this.multiFormatReader.decodeWithState(bitmap);
        } catch (ReaderException var17) {

        } finally {
            this.multiFormatReader.reset();
        }

        if(rawResult != null) {
            long end = System.currentTimeMillis();
            Log.d(TAG, "Found barcode (" + (end - start) + " ms):\n" + rawResult.toString());
            Message message = Message.obtain(this.activity.getHandler(), R.id.decode_succeeded, rawResult);
            Bundle bundle = new Bundle();
            bundle.putParcelable("barcode_bitmap", source.renderCroppedGreyscaleBitmap());
            message.setData(bundle);
            message.sendToTarget();
        } else {
            Message message = Message.obtain(this.activity.getHandler(), R.id.decode_failed);
            message.sendToTarget();
        }

    }
}

