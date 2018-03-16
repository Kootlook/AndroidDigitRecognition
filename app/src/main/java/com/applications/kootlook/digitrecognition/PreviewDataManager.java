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


class PreviewDataManager {

    static Bitmap getFrameContentsBitmap(byte[] data, Rect frameContentsRect, Point cameraResolution) {

        int frameWidth = frameContentsRect.width();
        int frameheight = frameContentsRect.height();
        int frameTop = frameContentsRect.top;
        int frameLeft = frameContentsRect.left;


        int[] frameData = new int[frameheight*frameWidth];

        int idx = 0;
        for (int h = 0; h < frameheight; h++) {
            int offset = (frameTop+h-1)*cameraResolution.x+frameLeft;
            for (int w = 0; w < frameWidth; w++) {

                //extract grey(Y channel of ) data from image and put it into ARGB_8888 format
                //the latter is stored in 4 bytes, a byte for each channel

                int grey = data[offset+w] & 0xff;
                frameData[idx++] = 0xFF000000 | (grey * 0x00010101);

            }
        }

        Bitmap bitmap = Bitmap.createBitmap(frameWidth, frameheight, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(frameData, 0, frameWidth, 0, 0, frameWidth, frameheight);


        return bitmap;

    }

}