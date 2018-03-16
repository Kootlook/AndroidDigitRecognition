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

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.view.Gravity;
import android.widget.Toast;


class PreviewCallback implements Camera.PreviewCallback {

    private Rect frameContentsRect;
    private NNet neuralNet;
    private FinderViewActivity finderViewActivity;
    private Point cameraResolution;

    PreviewCallback(Rect frameContentsRect, Point cameraResolution, NNet neuralNet, FinderViewActivity finderViewActivity) {

        this.frameContentsRect = frameContentsRect;
        this.cameraResolution = cameraResolution;
        this.neuralNet = neuralNet;
        this.finderViewActivity = finderViewActivity;


    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

        Bitmap frameContentsBitmap = PreviewDataManager.getFrameContentsBitmap(data, frameContentsRect, cameraResolution);
        RecognitionResult recognitionResult = neuralNet.recogniseDigit(frameContentsBitmap);

        if (!recognitionResult.recognitionIsSuccessful()) {
            Toast toast = Toast.makeText(finderViewActivity, "Digit recognition failed. Please try again.", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0, 0);
            toast.show();
            return;
        }else{
            String messageText = "The digit is "+recognitionResult.getRecognisedDigit()+"\n" +
                                         " I'm "+(int)(recognitionResult.getRecognisedDigitProbability()*10000)/100.+" % sure\n" +
                                         "Brightness gap is "+(recognitionResult.brightnessGapIsPresent()?"":"NOT")+" present";
            Toast toast = Toast.makeText(finderViewActivity, messageText, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0, 0);
            toast.show();

        }

        Bitmap greyBitmap784 = neuralNet.getBitmapFromArray(recognitionResult.getGreyValues(), 28, 28);
        // displaying processed frame picture for 3 seconds

        finderViewActivity.displayBitmapInFrameForAWhile(greyBitmap784, 3000);
        finderViewActivity.setShutterButtonClickable(true);

    }
}
