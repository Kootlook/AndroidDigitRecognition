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


final class RecognitionResult {

    private int recognisedDigit;
    private boolean recognitionSuccess;
    private double recognisedDigitProbability;
    private double[] probabilityVector;
    private boolean brightnessGapIsPresent;
    private int[] greyValues;

    RecognitionResult(int recognisedDigit, double recognisedDigitProbability, double[] probabilityVector, int[] greyValues,
                      boolean recognitionSuccess, boolean brightnessGapIsPresent) {
        this.recognisedDigit = recognisedDigit;
        this.recognisedDigitProbability = recognisedDigitProbability;
        this.probabilityVector = probabilityVector;
        this.greyValues = greyValues;
        this.recognitionSuccess = recognitionSuccess;
        this.brightnessGapIsPresent = brightnessGapIsPresent;
    }

    int getRecognisedDigit() {
        return recognisedDigit;
    }

    double getRecognisedDigitProbability() {
        return recognisedDigitProbability;
    }

    public double[] getProbabilityVector() {
        return probabilityVector;
    }

    boolean recognitionIsSuccessful() {
        return recognitionSuccess;
    }

    boolean brightnessGapIsPresent() {
        return brightnessGapIsPresent;
    }

    int[] getGreyValues() {
        return greyValues;
    }
}
