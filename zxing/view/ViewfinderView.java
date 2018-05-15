package com.chuangtu.theworldishere.zxing.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

import com.chuangtu.theworldishere.R;
import com.chuangtu.theworldishere.zxing.camera.CameraManager;
import com.google.zxing.ResultPoint;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by Buzz on 2018/1/17.
 */

public final class ViewfinderView extends View {
    private static final int[] SCANNER_ALPHA = new int[]{0, 64, 128, 192, 255, 192, 128, 64};
    private static final long ANIMATION_DELAY = 100L;
    private static final int OPAQUE = 255;
    private final Paint paint = new Paint();
    private Bitmap resultBitmap;
    private final int maskColor;
    private final int resultColor;
    private final int frameColor;
    private final int laserColor;
    private final int resultPointColor;
    private int scannerAlpha;
    private Collection<ResultPoint> possibleResultPoints;
    private Collection<ResultPoint> lastPossibleResultPoints;
    private boolean isFirst = false;
    private int slideTop;
    private int slideBottom;
    private int SPEEN_DISTANCE = 10;

    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources resources = this.getResources();
        this.maskColor = resources.getColor(R.color.viewfinder_mask);
        this.resultColor = resources.getColor(R.color.result_view);
        this.frameColor = resources.getColor(R.color.viewfinder_frame);
        this.laserColor = resources.getColor(R.color.viewfinder_laser);
        this.resultPointColor = Color.argb(1, 1, 1, 1);
        this.scannerAlpha = 0;
        this.possibleResultPoints = new HashSet(5);
    }

    public void onDraw(Canvas canvas) {
        Rect frame = CameraManager.get().getFramingRect();
        if(frame != null) {
            if(!this.isFirst) {
                this.isFirst = true;
                this.slideTop = frame.top;
                this.slideBottom = frame.bottom;
            }

            int width = canvas.getWidth();
            int height = canvas.getHeight();
            this.paint.setColor(this.resultBitmap != null?this.resultColor:this.maskColor);
            canvas.drawRect(0.0F, 0.0F, (float)width, (float)frame.top, this.paint);
            canvas.drawRect(0.0F, (float)frame.top, (float)frame.left, (float)(frame.bottom + 1), this.paint);
            canvas.drawRect((float)(frame.right + 1), (float)frame.top, (float)width, (float)(frame.bottom + 1), this.paint);
            canvas.drawRect(0.0F, (float)(frame.bottom + 1), (float)width, (float)height, this.paint);
            if(this.resultBitmap != null) {
                this.paint.setAlpha(255);
                canvas.drawBitmap(this.resultBitmap, (Rect)null, frame, this.paint);
            } else {
                int ScreenRate = 30;
                int CORNER_WIDTH = 5;
                this.paint.setColor(-16776961);
                canvas.drawRect((float)frame.left, (float)frame.top, (float)(frame.left + ScreenRate), (float)(frame.top + CORNER_WIDTH), this.paint);
                canvas.drawRect((float)frame.left, (float)frame.top, (float)(frame.left + CORNER_WIDTH), (float)(frame.top + ScreenRate), this.paint);
                canvas.drawRect((float)(frame.right - ScreenRate), (float)frame.top, (float)frame.right, (float)(frame.top + CORNER_WIDTH), this.paint);
                canvas.drawRect((float)(frame.right - CORNER_WIDTH), (float)frame.top, (float)frame.right, (float)(frame.top + ScreenRate), this.paint);
                canvas.drawRect((float)frame.left, (float)(frame.bottom - CORNER_WIDTH), (float)(frame.left + ScreenRate), (float)frame.bottom, this.paint);
                canvas.drawRect((float)frame.left, (float)(frame.bottom - ScreenRate), (float)(frame.left + CORNER_WIDTH), (float)frame.bottom, this.paint);
                canvas.drawRect((float)(frame.right - ScreenRate), (float)(frame.bottom - CORNER_WIDTH), (float)frame.right, (float)frame.bottom, this.paint);
                canvas.drawRect((float)(frame.right - CORNER_WIDTH), (float)(frame.bottom - ScreenRate), (float)frame.right, (float)frame.bottom, this.paint);
                this.slideTop += this.SPEEN_DISTANCE;
                if(this.slideTop >= frame.bottom) {
                    this.slideTop = frame.top;
                }

                Rect lineRect = new Rect();
                lineRect.left = frame.left;
                lineRect.right = frame.right;
                lineRect.top = this.slideTop;
                lineRect.bottom = this.slideTop + 18;
                canvas.drawBitmap(((BitmapDrawable)((BitmapDrawable)this.getResources().getDrawable(R.drawable.fle))).getBitmap(), (Rect)null, lineRect, this.paint);
                this.paint.setColor(this.laserColor);
                this.paint.setAlpha(SCANNER_ALPHA[this.scannerAlpha]);
                this.scannerAlpha = (this.scannerAlpha + 1) % SCANNER_ALPHA.length;
                int middle = frame.height() / 2 + frame.top;
                canvas.drawRect((float)(frame.left + 2), (float)(middle - 1), (float)(frame.right - 1), (float)(middle + 2), this.paint);
                Collection<ResultPoint> currentPossible = this.possibleResultPoints;
                Collection<ResultPoint> currentLast = this.lastPossibleResultPoints;
                Iterator var11;
                ResultPoint point;
                if(currentPossible.isEmpty()) {
                    this.lastPossibleResultPoints = null;
                } else {
                    this.possibleResultPoints = new HashSet(5);
                    this.lastPossibleResultPoints = currentPossible;
                    this.paint.setAlpha(255);
                    this.paint.setColor(this.resultPointColor);
                    var11 = currentPossible.iterator();

                    while(var11.hasNext()) {
                        point = (ResultPoint)var11.next();
                        canvas.drawCircle((float)frame.left + point.getX(), (float)frame.top + point.getY(), 6.0F, this.paint);
                    }
                }

                if(currentLast != null) {
                    this.paint.setAlpha(127);
                    this.paint.setColor(this.resultPointColor);
                    var11 = currentLast.iterator();

                    while(var11.hasNext()) {
                        point = (ResultPoint)var11.next();
                        canvas.drawCircle((float)frame.left + point.getX(), (float)frame.top + point.getY(), 3.0F, this.paint);
                    }
                }

                this.postInvalidateDelayed(100L, frame.left, frame.top, frame.right, frame.bottom);
            }

        }
    }

    public void drawViewfinder() {
        this.resultBitmap = null;
        this.invalidate();
    }

    public void drawResultBitmap(Bitmap barcode) {
        this.resultBitmap = barcode;
        this.invalidate();
    }

    public void addPossibleResultPoint(ResultPoint point) {
        this.possibleResultPoints.add(point);
    }
}

