package com.chuangtu.theworldishere.zxing.decoding;

import android.app.Activity;
import android.content.DialogInterface;

/**
 * Created by Buzz on 2018/1/17.
 */

public final class FinishListener implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener, Runnable {
    private final Activity activityToFinish;

    public FinishListener(Activity activityToFinish) {
        this.activityToFinish = activityToFinish;
    }

    public void onCancel(DialogInterface dialogInterface) {
        this.run();
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        this.run();
    }

    public void run() {
        this.activityToFinish.finish();
    }
}

