package com.mobge.simpleimageprocessing;

import java.awt.image.BufferedImage;

public abstract class BaseBufferedFilter extends Block {
    ImageData _dataBuffer;
    public BaseBufferedFilter(int width, int height) {
        _dataBuffer = new ImageData(width, height);
        _dataBuffer.clear(1,0,0,0);

    }

    /**
     * @throws IllegalStateException if dimension of the image is different than the buffer.
     */
    void assertSize(ImageData image) {
        int w = _dataBuffer.getWidth();
        int h = _dataBuffer.getHeight();
        if(w != image.getWidth() || h != image.getHeight()){
            throw new IllegalStateException("dimensions of given image are not matched with dimensions of this " + getClass().toString());
        }
    }

    public void convertToDifferences(ImageData data) {


        float[] result = _dataBuffer.getRawData();
        float[] source = data.getRawData();
        int length = source.length;
        for(int i = 0; i < length;) {
            result[i] = Math.abs(source[i] - result[i]);
            i++;
            result[i] = Math.abs(source[i] - result[i]);
            i++;
            result[i] = Math.abs(source[i] - result[i]);
            i++;

            i++;
        }
    }

    void copyRedToGreenBlue() {

        copyRedToGreenBlue(_dataBuffer);
    }

    public BufferedImage toBufferedImage() {
        return _dataBuffer.toBufferedImage();
    }

    public ImageData getBuffer() {
        return _dataBuffer;
    }
}