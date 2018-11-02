package com.mobge.simpleimageprocessing;

public class ScaleValuesFilter extends BaseBufferedFilter{
    private Configuration _configuration;
    private float[] _xScl, _yScl;
    public ScaleValuesFilter(int width, int height) {
        super(width, height);
        _xScl = new float[width];
        _yScl = new float[height];

        float pi = (float)Math.PI;
        float xStep = pi/(float)width;
        float yStep = pi/(float)height;
        float xR = -pi*0.5f;
        float yR = -pi*0.5f;
        for(int i = 0; i < width; i++, xR += xStep) {
            _xScl[i] = 1.0f / (float)Math.cos(xR);
        }
        for(int i = 0; i < height; i++, yR += yStep) {
            _yScl[i] = 1.0f / (float)Math.cos(yR);
        }
    }
    public void setConfiguration(Configuration configuration){
        _configuration = configuration;
    }

    @Override
    public void performOperation() {
        ImageData input = getInput(0);
        assertSize(input);
        ImageData output = _dataBuffer;

        float[] fInput = input.getRawData();
        float[] fOutput = output.getRawData();

        switch (_configuration.mode) {
            case ScaleWithConstant: {
                float constant = _configuration.scaleConstant;
                for (int index = ImageData.OFFSET_R; index < fInput.length; index += ImageData.BYTES_PER_PIXEL) {
                    fOutput[index] = fInput[index] * constant;
                }
            }
            break;
            case ReverseScaleSphericalTransform: {
                float constant = _configuration.scaleConstant;
                int w = input.getWidth();
                int h = input.getHeight();
                int bbp = ImageData.BYTES_PER_PIXEL;
                int offset = ImageData.OFFSET_R;
                for(int y = 0; y < h; y++){
                    float yScl = _yScl[y];
                    for(int x = 0; x < w; x++, offset += bbp){
                        float xScl = _xScl[x];
                        fOutput[offset] = fInput[offset] * xScl * yScl * constant;
                    }
                }
            }
            break;
        }
        copyRedToGreenBlueIfOut();
        sendOutput(output, 0);
    }

    public static class Configuration {
        public float scaleConstant;
        public Mode mode;
        public static Configuration constantScale(float constant) {
            Configuration f = new Configuration();
            f.mode = Mode.ScaleWithConstant;
            f.scaleConstant = constant;
            return f;
        }

        public static Configuration reverseSphericalTransform(float constant) {
            Configuration f = new Configuration();
            f.mode = Mode.ReverseScaleSphericalTransform;
            f.scaleConstant = constant;
            return f;
        }
    }
    public enum Mode{
        ScaleWithConstant,
        ReverseScaleSphericalTransform,
    }
}
