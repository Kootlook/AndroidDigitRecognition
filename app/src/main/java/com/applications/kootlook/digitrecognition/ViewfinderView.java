//Copyright (c) <2018> <Arislan Makhmudov>
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//        of this software and associated documentation files (the "Software"), to deal
//        in the Software without restriction, including without limitation the rights
//        to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//        copies of the Software, and to permit persons to whom the Software is
//        furnished to do so, subject to the following conditions:
//
//        The above copyright notice and this permission notice shall be included in all
//        copies or substantial portions of the Software.
//
//        THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//        IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//        FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//        AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//        LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//        OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
//        SOFTWARE.

package com.applications.kootlook.digitrecognition;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;



final class ViewfinderView extends android.support.v7.widget.AppCompatImageView {

    private int halfSize;
    private int lineThickness;
    private Paint maskPaint;
    private Paint transparentPaint;
    private Paint framePaint;
    private Point centerOfScreen;
    private int maxHalfSize, minHalfSize;
    private Rect frameContentsRect;
    private Point screenResolution;
    private Point cameraResolution;

    public ViewfinderView(Context context, @Nullable AttributeSet attrs) {

        super(context, attrs);
        halfSize = 0;

        maskPaint = new Paint();
        maskPaint.setColor(getResources().getColor(R.color.viewfinder_mask));

        transparentPaint = new Paint();
        transparentPaint.setColor(Color.TRANSPARENT);
        transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        transparentPaint.setAntiAlias(true);

        framePaint = new Paint();
        framePaint.setColor(getResources().getColor(R.color.viewfinder_frame));

        lineThickness = 2;
        minHalfSize = 28;
        maxHalfSize = Integer.MAX_VALUE;
        frameContentsRect = new Rect();

    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (halfSize <= 0)
            return;

        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), maskPaint);

        //filling the frame with the viewfinder_frame color
        canvas.drawRect(centerOfScreen.x - halfSize - lineThickness, centerOfScreen.y - halfSize - lineThickness,
                centerOfScreen.x + halfSize + lineThickness, centerOfScreen.y + halfSize + lineThickness, framePaint);

        //contracting the frame area to make it transparent
        frameContentsRect.set(centerOfScreen.x - halfSize, centerOfScreen.y - halfSize,
                centerOfScreen.x + halfSize, centerOfScreen.y + halfSize);

        //making the current frame area transparent
        canvas.drawRect(frameContentsRect, transparentPaint);

        super.onDraw(canvas);
    }

    public void drawYourself(int size) {

        halfSize = size / 2;


        if (halfSize < minHalfSize) {
            //if the finger is trying to make the frame smaller than minimal size do nothing
            halfSize = minHalfSize;
            return;
        }else if (halfSize > maxHalfSize){
            //if the finger is trying to enlarge the frame beyond available area on screen do nothing
            halfSize = maxHalfSize;
            return;
        }

        invalidate();

    }

    public int distanceToCenterX(Point point) {

        return Math.abs(point.x - centerOfScreen.x);

    }

    public int distanceToCenterY(Point point) {

        return Math.abs(point.y - centerOfScreen.y);

    }

    public boolean pointIsNearFrameX(Point point) {

        //point is on the right or left border of the frame
        return Math.abs(distanceToCenterX(point) - halfSize) < 15
                       && distanceToCenterY(point) <= halfSize;

    }

    public boolean pointIsNearFrameY(Point point) {

        //point is on the top or bottom border of the frame
        return Math.abs(distanceToCenterY(point) - halfSize) < 15
                       && distanceToCenterX(point) <= halfSize;

    }

    public Rect getRectScaledFromScreenToCamera(){

        //we have to scale the rectangle frame from screen resolution to camera
        float xFactor = ((float) cameraResolution.x)/screenResolution.x;
        float yFactor = ((float) cameraResolution.y)/screenResolution.y;

        //center of viewfinder view
        Point centerOfCameraVF = new Point(cameraResolution.x / 2, cameraResolution.y / 2);
        int left = (int) (xFactor * (frameContentsRect.left - centerOfScreen.x) + centerOfCameraVF.x);
        int right = (int) (xFactor * (frameContentsRect.right - centerOfScreen.x) + centerOfCameraVF.x);
        int top = (int) (yFactor * (frameContentsRect.top - centerOfScreen.y) + centerOfCameraVF.y);
        int bottom = (int) (yFactor * (frameContentsRect.bottom - centerOfScreen.y) + centerOfCameraVF.y);

        return new Rect(left, top, right, bottom);

    }


    public void setCenterOfScreen(Point centerOfScreen) {
        this.centerOfScreen = centerOfScreen;
    }

    public void setScreenResolution(Point screenResolution) {
        this.screenResolution = screenResolution;
        maxHalfSize = Math.min(screenResolution.x, screenResolution.y)/2;
    }

    public void setCameraResolution(Point cameraResolution) {
        this.cameraResolution = cameraResolution;
    }

    public void setImageBitmapIntoFrame(Bitmap bitmap){

        Bitmap bmpScaled = Bitmap.createScaledBitmap(bitmap, frameContentsRect.width(), frameContentsRect.height(), true);
        bitmap.recycle();

        Bitmap fullScreen = Bitmap.createBitmap(screenResolution.x, screenResolution.y, Bitmap.Config.ARGB_8888);
        fullScreen.setHasAlpha(true);

        Canvas canvas = new Canvas(fullScreen);
        canvas.drawBitmap(bmpScaled, frameContentsRect.left, frameContentsRect.top, null);
        bmpScaled.recycle();

        setImageBitmap(fullScreen);

    }
    public void clearFrameBitmap(){

        setImageResource(android.R.color.transparent);

    }


}
