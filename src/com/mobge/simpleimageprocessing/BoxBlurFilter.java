package com.mobge.simpleimageprocessing;


public class BoxBlurFilter extends BaseBufferedFilter {


    private Configuration _configuration;

    private ImageData _dataBuffer2;

    public BoxBlurFilter(int width, int height) {
        super(width, height);
        _dataBuffer2 = new ImageData(width, height);
    }
    public BoxBlurFilter setConfiguration(Configuration configuration){
        _configuration = configuration;
        return this;
    }

    @Override
    public void performOperation() {
        ImageData image = getInput(0);
        assertSize(image);
        _dataBuffer.copyFrom(image);
        for(int i = 0; i < _configuration.repeat; i++){
            blur();
        }

        copyRedToGreenBlue();
        sendOutput(_dataBuffer, 0);
    }

    private void swapBuffers() {
        ImageData temp = _dataBuffer2;
        _dataBuffer2 = _dataBuffer;
        _dataBuffer = temp;
    }

    private void blur() {
        int w = _dataBuffer.getWidth();
        int h = _dataBuffer.getHeight();
        int r = _configuration.radius;
        accumH(w, h, r);
        accumV(w, h, r);
    }
    private void accumV(int w, int h, int r) {
        final int bbp = ImageData.BYTES_PER_PIXEL;
        final int stride = w * bbp;
        accum(w, h, bbp, stride, r);
    }
    private void accumH(int w, int h, int r) {
        final int bbp = ImageData.BYTES_PER_PIXEL;
        final int stride = w * bbp;
        accum(h, w, stride, bbp, r);
    }
    private void accum(int rowCount, int columnCount,
                       int offStep, int indexStep,
                       int radius) {
        float[] source = _dataBuffer.getRawData();
        float[] target = _dataBuffer2.getRawData();
        final float normalizer = 1.0f / (radius + radius + 1);
        final int bR = radius * indexStep;
        final int stride = columnCount * indexStep;
        int endOffset = rowCount * offStep + ImageData.OFFSET_R;
        for (int offset = ImageData.OFFSET_R; offset < endOffset; offset += offStep) {
            float firstValue = source[offset];
            // assume off screen values are the same with pixel on the edge
            // and predict accumulation value according to that
            float accumulation = firstValue * radius;
            int i = offset;
            int end = offset + bR;
            for (; i < end; i += indexStep) {
                accumulation += source[i];
            }

            // calculate blur for range x = 0 -> r
            i = offset;
            for (; i < end; i += indexStep) {
                accumulation += source[i + bR];
                target[i] = accumulation * normalizer;
                accumulation -= firstValue;
            }

            // calculate blur for range x = r -> (width - r)
            end = stride - bR + offset;
            for (; i < end; i += indexStep) {
                accumulation += source[i + bR];
                target[i] = accumulation * normalizer;
                accumulation -= source[i - bR];
            }

            // calculate blur for range x = (width - r) -> width
            end = offset + stride;
            float lastValue = source[end - indexStep];
            for (; i < end; i += indexStep) {
                accumulation += lastValue;
                target[i] = accumulation * normalizer;
                accumulation -= source[i - bR];
            }
        }
        swapBuffers();
    }


    public static class Configuration {
        int radius;
        int repeat;

        /**
         * Creates a configuration object to perform a box blur.
         * @param radius a radius for box blur operation. One dimension of box is
         *               calculated as (2 * radius + 1).
         * @param repeat repeat count that box blur operation will be performed.
         *               Multiple passes of the box blur approximates to gaussian blur.
         */
        public Configuration(int radius, int repeat){
            this.radius = radius;
            this.repeat = repeat;
        }
    }
}
