package com.mobge.simpleimageprocessing;


import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageData {
    public static final int OFFSET_A = 3;
    public static final int OFFSET_R = 2;
    public static final int OFFSET_G = 1;
    public static final int OFFSET_B = 0;
    public static final int BYTES_PER_PIXEL = 4;


    private float[] _data;
    private int _width, _height;

    private int _position;
    // private Producer _producer;


    public ImageData(float[] data, int width, int height) {
        this._data = data;
        this._width = width;
        this._height = height;
    }
    public ImageData(int width, int height){
        setDimensions(width, height);
    }

    public int getOffset(int x, int y){
        return (y* _width +x)*4;
    }
    public void setPosition(int x, int y){
        _position = getOffset(x,y);
    }

    public float red(){
        return _data[_position+OFFSET_R];
    }
    public float blue(){
        return _data[_position+OFFSET_B];
    }
    public float green(){
        return _data[_position+OFFSET_G];
    }
    public float alpha(){
        return _data[_position+OFFSET_A];
    }

    public float[] getRawData() {
        return _data;
    }

    public void clear(float a, float r, float g, float b){
        for (int i = 0; i < _data.length; i+=4) {
            _data[i + OFFSET_A] = a;
            _data[i + OFFSET_R] = r;
            _data[i + OFFSET_G] = g;
            _data[i + OFFSET_B] = b;
        }
    }

    public void copyFrom(ImageData image){
        int w = getWidth();
        int h = getHeight();
        if(w != image.getWidth() || h != image.getHeight()){
            throw new IllegalStateException("dimensions of given image are not matched with dimensions of this " + getClass().toString());
        }
        float[] source = image.getRawData();
        for(int i = 0; i < _data.length; i++){
            _data[i] = source[i];
        }
    }

    public BufferedImage toBufferedImage() {
        BufferedImage image = new BufferedImage(this._width, this._height, BufferedImage.TYPE_INT_ARGB);
        int[] ints = new int[_width * _height];
        int offset = 0;
        for (int i = 0; i < ints.length; i++, offset += 4) {
            ints[i] = toInt(
                    (byte)(_data[offset + OFFSET_A]*255f),
                    (byte)(_data[offset + OFFSET_R]*255f),
                    (byte)(_data[offset + OFFSET_G]*255f),
                    (byte)(_data[offset + OFFSET_B]*255f)
            );
        }

        image.setRGB(0, 0, this._width, this._height, ints, 0, this._width);
        return image;
    }
    public BufferedImage toResizedBufferedImage(int width, int height) {
        BufferedImage bi = toBufferedImage();
        if(width == _width && height == _height){
            return bi;
        }
        Image i = bi.getScaledInstance(width, height, bi.getType());

        BufferedImage result = new BufferedImage(width, height, bi.getType());
        Graphics g = result.getGraphics();
        g.drawImage(i,0,0, null);
        g.dispose();
        return result;
    }



    public int getWidth() {
        return _width;
    }

    public int getHeight() {
        return _height;
    }

    private static int toInt(byte a, byte r, byte g, byte b) {
        return ((a&0xff) << 24) |
                ((r&0xff) << 16) |
                ((g&0xff) << 8) |
                ((b&0xff) << 0);
    }
    private void setDimensions(int width, int height) {
        if(this._width != width && this._height != height){
            this._width = width;
            this._height = height;
            int size = width*height*4;
            if(_data == null || _data.length!= size){
                _data = new float[size];
            }
        }
    }

}
