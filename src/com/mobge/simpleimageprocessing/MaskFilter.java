package com.mobge.simpleimageprocessing;


public class MaskFilter extends BaseBufferedFilter {

    public static final int INPUT_MASK = 0;
    public static final int INPUT_SOURCE = 1;

    private Configuration _configuration;

    public MaskFilter(Configuration configuration, int width, int height) {
        super(width, height);
        _configuration = configuration;
    }

    @Override
    public int numberOfInputs() {
        return 2;
    }

    @Override
    public void performOperation() {
        ImageData imask = getInput(INPUT_MASK);
        ImageData isource = getInput(INPUT_SOURCE);

        float minColor = _configuration.minMaskColor;
        float maxColor = _configuration.maxMaskColor;

        float[] mask = imask.getRawData();
        float[] source = isource.getRawData();
        float[] result = _dataBuffer.getRawData();

        final int bbp = ImageData.BYTES_PER_PIXEL;

        switch (_configuration.mode){
            case MaskBetween:

                for(int offset = ImageData.OFFSET_R; offset < source.length; offset += bbp) {
                    float mval = mask[offset];
                    result[offset] = (minColor <= mval && mval <= maxColor) ? mval : source[offset];
                }

                break;
                default:

                    for(int offset = ImageData.OFFSET_R; offset < source.length; offset += bbp) {
                        float mval = mask[offset];
                        result[offset] = (minColor <= mval && mval <= maxColor) ? source[offset] : mval;
                    }

                    break;
        }
        copyRedToGreenBlue();
        sendOutput(_dataBuffer, 0);
    }

    public static class Configuration {
        float minMaskColor, maxMaskColor;
        Mode mode;
        public Configuration(Mode mode, float minMaskColor, float maxMaskColor){
            this.minMaskColor = minMaskColor;
            this.maxMaskColor = maxMaskColor;
            this.mode = mode;
        }
    }
    public enum Mode{
        MaskBetween,
        MaskOutside,
    }
}
