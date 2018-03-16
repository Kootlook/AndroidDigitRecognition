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

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;


public final class FinderViewActivity extends Activity implements SurfaceHolder.Callback{

    private Button shutterButton;
    private Camera camera;
    private boolean cameraIsOn;
    private boolean assistedAutoFocusIsInAction;
    private Point startingTouchLocation;
    private ViewfinderView viewfinderView;
    private Point cameraResolution;
    private NNet neuralNet;
    private FinderViewActivity finderViewActivity;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.finder_view);

        shutterButton = findViewById(R.id.shutter_button);
        shutterButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if (cameraIsOn){
                    finderViewActivity.setShutterButtonClickable(false);
                    camera.setOneShotPreviewCallback(new PreviewCallback(viewfinderView.getRectScaledFromScreenToCamera(),
                                                                                cameraResolution, neuralNet,
                                                                                finderViewActivity));
                }
            }
        });

        cameraIsOn = false;
        assistedAutoFocusIsInAction = false;
        neuralNet = NNet.getInstance(this);
        finderViewActivity = this;

        SurfaceView surfaceView = findViewById(R.id.previewView);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

        viewfinderView = findViewById(R.id.viewfinderView);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (!cameraIsOn) return super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startingTouchLocation = new Point((int)event.getX(), (int)event.getY());
                break;
            case MotionEvent.ACTION_UP:
                double distance = Math.sqrt(Math.pow(event.getX()-startingTouchLocation.x,2)+
                                                   Math.pow(event.getX()-startingTouchLocation.x,2));


                //if the distance is less than 10 px and touching point is not in
                // the neighborhood of rectangular frame, then request focus from camera

                if (distance<10 && cameraIsOn && !assistedAutoFocusIsInAction){

                    assistedAutoFocusIsInAction = true;
                    camera.autoFocus(new Camera.AutoFocusCallback(){

                        @Override
                        public void onAutoFocus(boolean success, Camera camera) {
                            assistedAutoFocusIsInAction = false;
                        }
                    });
                }

                break;

            case MotionEvent.ACTION_MOVE:
                Point cLocation = new Point((int)event.getX(), (int)event.getY());

                if(viewfinderView.pointIsNearFrameX(cLocation)){
                    //the finger or whatever is touching the screen is on frame's right or left border
                    viewfinderView.drawYourself(2*viewfinderView.distanceToCenterX(cLocation));
                }else if(viewfinderView.pointIsNearFrameY(cLocation)){
                    //the finger or whatever is touching the screen is on frame's top or bottom border
                    viewfinderView.drawYourself(2*viewfinderView.distanceToCenterY(cLocation));
                }

                break;
        }

        return super.onTouchEvent(event);
    }

    public void setShutterButtonClickable(boolean clickable) {
        this.shutterButton.setClickable(clickable);
    }

    public void displayBitmapInFrameForAWhile(Bitmap cameraBitmap, int milliSeconds){

        viewfinderView.setImageBitmapIntoFrame(cameraBitmap);

        new UIThread(this, milliSeconds, new Runnable() {
            @Override
            public void run() {
                viewfinderView.clearFrameBitmap();
            }
        }).start();

    }

    //--------------------SurfaceHolder.Callback--------------------
    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        if(!cameraIsOn){
            camera = Camera.open();
            Camera.Parameters parameters = camera.getParameters();
            List<String> focusModes = parameters.getSupportedFocusModes();

            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)){
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }

            Camera.Size defaultSize = parameters.getPreviewSize();
            parameters.setPreviewSize(defaultSize.width, defaultSize.height);
            cameraResolution = new Point(defaultSize.width, defaultSize.height);

            WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            Point screenResolution = new Point(display.getWidth(), display.getHeight());

            viewfinderView.setCenterOfScreen(new Point(screenResolution.x/2, screenResolution.y/2));
            viewfinderView.setCameraResolution(cameraResolution);
            viewfinderView.setScreenResolution(screenResolution);

            try {
                camera.setPreviewDisplay(holder);
                camera.setParameters(parameters);
                camera.startPreview();

                cameraIsOn = true;

                viewfinderView.drawYourself(Math.min(screenResolution.x, screenResolution.y) / 2);

            }catch (RuntimeException e){
                Toast.makeText(this, "Couldn't start camera preview. Please restart the application",Toast.LENGTH_SHORT).show();
            }catch (IOException e){
                Toast.makeText(this, "Couldn't start camera preview. Please restart the application",Toast.LENGTH_SHORT).show();
            }

        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        cameraIsOn = false;
        camera.stopPreview();
        camera.release();
    }
}


