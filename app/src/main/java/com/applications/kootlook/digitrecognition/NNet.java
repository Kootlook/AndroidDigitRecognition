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
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

final class NNet {

    private static NNet uniqueInstance;
    private final String CLASS_TAG = NNet.class.getSimpleName();


    //first element contains 30 rows belonging to the first layer of neural net, each row contains 784 elements
    //second element contains 10 rows belonging to the second layer of neural net, each row contains 30 elements
    private final double[][][] weights;

    //first element contains 30 bias values belonging to the first layer of neural net
    //second element contains 10 bias values belonging to the second layer of neural net

    private final double[][] biases;

    private NNet(Context context) {
        //reading weights and biases data from Assets

        IOOperations io = new IOOperations();

        ArrayList<String> strWeights = io.readDataFromAssets(context, "weights.txt");
        double[][][] weights = {new double[30][784] , new double[10][30]};

        ArrayList<String> strBiases = io.readDataFromAssets(context, "biases.txt");
        double[][] biases = {new double[30] , new double[10]};

        int listIdx = 0;
        for(int layerIdx = 0, len1 = weights.length; layerIdx < len1; layerIdx ++){
            int len2 = weights[layerIdx].length;
            for(int rowIdx = 0; rowIdx < len2; rowIdx ++){
                biases[layerIdx][rowIdx] = Double.parseDouble(strBiases.get(listIdx));
                String[] elements = strWeights.get(listIdx).split(" ");
                int len3 = weights[layerIdx][rowIdx].length;
                for(int elemIdx = 0; elemIdx < len3; elemIdx ++){
                    weights[layerIdx][rowIdx][elemIdx] = Double.parseDouble(elements[elemIdx]);
                }
                listIdx++;
            }
        }

        this.weights = weights;
        this.biases = biases;

    }

    static NNet getInstance(Context context) {
        if (uniqueInstance == null) {
            uniqueInstance = new NNet(context);
        }
        return uniqueInstance;
    }


    private double dotProduct(double[] vector1, double[] vector2){

        if(Double.isNaN(vector1.length)){
            Log.e(CLASS_TAG+" dotProduct", "Can't take the dot product of two vectors of different lengths "+vector1.length+" and "+vector2.length );
            return Double.POSITIVE_INFINITY;
        }

        if(vector1.length == 0){
            Log.e(CLASS_TAG+" dotProduct", "Can't take the dot product of two empty vectors");
            return Double.POSITIVE_INFINITY;
        }

        double dotProduct = 0;
        for(int i = 0; i < vector1.length; i++){

            dotProduct += vector1[i]*vector2[i];

        }

        return dotProduct;

    }

    private double sigmoid(double number){

        return 1/(1+Math.exp(-number));

    }

    Bitmap getBitmapFromArray(int[] greyValueArray, int width, int height){


        int len = width*height;

        if (len < greyValueArray.length){
            throw new ArrayIndexOutOfBoundsException("Array passed in does not fit into pixel frame");
        }

        int[] pixelData = new int[len];
        for (int idx = 0; idx < len; idx++) {

            int grey = greyValueArray[idx];
            pixelData[idx] = 0xFF000000 | (grey * 0x00010101);

        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixelData, 0, width, 0, 0, width, height);

        return bitmap;


    }

    RecognitionResult recogniseDigit(Bitmap bmpOriginal){

        Bitmap bmpScaled = Bitmap.createScaledBitmap(bmpOriginal, 28, 28, true);

        ByteBuffer buffer = ByteBuffer.allocate(bmpScaled.getByteCount());
        bmpScaled.copyPixelsToBuffer(buffer);
        byte[] list_of_pixels = buffer.array();

        int[] list_of_gray_pixels = new int[784];

        int idx = 0;
        for (int i = 0; i < 784; i++) {

            //we know that R,G, and B channels contain the same numbers
            int B = list_of_pixels[idx++];
            idx+=3;//so skip G,R and A channels
            //neural net is trained on 0:255 range thus shifting the grey value by 128
            //otherwise the range will be -128:127
            list_of_gray_pixels[i] = B + 128;
        }

        int[] sortedPixels = Arrays.copyOf(list_of_gray_pixels, 784);
        Arrays.sort(sortedPixels);

        boolean brightnessGapExists = false;
        double threshholdBrightness = 0;
        for (int i = 1; i < 784; i++) {

            if (sortedPixels[i - 1] + 100 < sortedPixels[i]) {
                threshholdBrightness = sortedPixels[i - 1] + 1;
                brightnessGapExists = true;
                break;
            }

        }

        double[] input = new double[784];
        //turn image pixels to black or white
        if (brightnessGapExists) {
            for (int i = 0; i < 784; i++) {
                if (list_of_gray_pixels[i] < threshholdBrightness) {
                    //make it pure black
                    input[i] = 0;
                    list_of_gray_pixels[i] = 0;
                } else {
                    //make it pure white
                    input[i] = 1.;
                    list_of_gray_pixels[i] = 255;
                }
            }
        }else{
            //there's no contrast in the picture
            //normalisation of the brightness values
            for (int i = 0; i < 784; i++) {
                input[i] = list_of_gray_pixels[i]/255.;
            }
        }


        double[] outputVector = {};
        double[] inputVector = Arrays.copyOf(input, input.length);

        for(int layerIdx = 0, len1 = weights.length; layerIdx < len1; layerIdx ++){
            int len2 = weights[layerIdx].length;
            outputVector = new double[len2];
            for(int rowIdx = 0; rowIdx < len2; rowIdx ++){
                double[] weightVector = weights[layerIdx][rowIdx];
                double bias = biases[layerIdx][rowIdx];
                double dProduct = dotProduct(inputVector, weightVector);
                if(dProduct < Double.POSITIVE_INFINITY)
                    outputVector[rowIdx] = sigmoid(dProduct + bias);
                else
                    return new RecognitionResult(-1,0., new double[]{}, new int[]{}
                                                        , false, brightnessGapExists);
            }

            inputVector = Arrays.copyOf(outputVector, outputVector.length);

        }

        int maxIdx = 0;
        double maxValue = 0;
        for(idx = 0; idx < outputVector.length; idx++ ){

            if(maxValue < outputVector[idx]){
                maxValue = outputVector[idx];
                maxIdx = idx;
            }

        }

        int recognisedDigit = maxIdx;
        return new RecognitionResult(recognisedDigit, outputVector[maxIdx], outputVector, list_of_gray_pixels
                                            ,true, brightnessGapExists);

    }


}
