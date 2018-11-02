package com.mobge.simpleimageprocessing;


public class GrowFilter extends BaseBufferedFilter{
    private Configuration _configuration;
    private int[] _binaryBuffer;
    public GrowFilter(Configuration configuration, int width, int height) {
        super(width, height);
        _configuration = configuration;
        _binaryBuffer = new int[width*height*ImageData.BYTES_PER_PIXEL];
    }

    @Override
    public void performOperation() {
        ImageData input = getInput(0);
        assertSize(input);
        int w = _dataBuffer.getWidth();
        int h = _dataBuffer.getHeight();
        initBinaryBuffer(input);
        accumV(w, h, _configuration.radius);
        initBinaryBuffer(_dataBuffer);
        accumH(w, h, _configuration.radius);
        copyRedToGreenBlue(_dataBuffer);
        sendOutput(_dataBuffer, 0);
    }

    private void initBinaryBuffer(ImageData source) {

        float minColor = _configuration.minGrowColor;
        float maxColor = _configuration.maxGrowColor;

        float[] raw = source.getRawData();
        for (int offset = ImageData.OFFSET_R; offset < raw.length; offset += ImageData.BYTES_PER_PIXEL) {
            float val = raw[offset];
            _binaryBuffer[offset] = (minColor <= val && val <= maxColor) ? 1 : 0;
        }
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
        float minColor = _configuration.minGrowColor;
        float[] target = _dataBuffer.getRawData();
        float[] source = ((ImageData)getInput(0)).getRawData();
        final int bR = radius * indexStep;
        final int stride = columnCount * indexStep;
        int endOffset = rowCount * offStep + ImageData.OFFSET_R;
        int minNeighbourCount = _configuration.minNeighbourCount;
        for (int offset = ImageData.OFFSET_R; offset < endOffset; offset += offStep) {
            int firstValue = _binaryBuffer[offset];

            // assume off screen values are the same with pixel on the edge
            // and predict accumulation value according to that
            int accumulation = firstValue * radius;
            int i = offset;
            int end = offset + bR;
            for (; i < end; i += indexStep) {

                accumulation += _binaryBuffer[offset];
            }

            // calculate blur for range x = 0 -> r
            i = offset;
            for (; i < end; i += indexStep) {
                accumulation += _binaryBuffer[i + bR];
                target[i] = accumulation >= minNeighbourCount ? minColor : source[i];
                accumulation -= firstValue;
            }

            // calculate blur for range x = r -> (width - r)
            end = stride - bR + offset;
            for (; i < end; i += indexStep) {
                accumulation += _binaryBuffer[i + bR];
                target[i] = accumulation >= minNeighbourCount ? minColor : source[i];
                accumulation -= _binaryBuffer[i - bR];
            }

            // calculate blur for range x = (width - r) -> width
            end = offset + stride;
            float lastValue = _binaryBuffer[end - indexStep];
            for (; i < end; i += indexStep) {
                accumulation += lastValue;
                target[i] = accumulation >= minNeighbourCount ? minColor : source[i];
                accumulation -= _binaryBuffer[i - bR];
            }
        }
    }

    public static class Configuration{
        public int radius;
        public float minGrowColor;
        public float maxGrowColor;
        public int minNeighbourCount;

        public Configuration(int radius, float minGrowColor, float maxGrowColor, int minNeighbourCount) {
            this.minGrowColor = minGrowColor;
            this.maxGrowColor = maxGrowColor;
            this.radius = radius;
            this.minNeighbourCount = minNeighbourCount;
        }
    }
}
