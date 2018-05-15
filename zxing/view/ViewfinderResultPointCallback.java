package com.chuangtu.theworldishere.zxing.view;

import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;


/**
 * Created by Buzz on 2018/1/17.
 */

public final class ViewfinderResultPointCallback implements ResultPointCallback {
    private final ViewfinderView viewfinderView;

    public ViewfinderResultPointCallback(ViewfinderView viewfinderView) {
        this.viewfinderView = viewfinderView;
    }

    public void foundPossibleResultPoint(ResultPoint point) {
        this.viewfinderView.addPossibleResultPoint(point);
    }
}

