package com.mobge.simpleimageprocessing;


import java.util.Arrays;

public class DivideFilter extends BaseBufferedFilter {
    private int[] _counts;
    private int[] _cumulativeCounts;
    private float _min, _max;
    private int _rangeStart, _rangeCount;

    private Configuration _conf;


    public DivideFilter(int width, int height) {
        super(width, height);
    }

    private void resetCounts() {
        int i;
        for(i = 0; i < _counts.length; i++){
            _counts[i] = 0;
            _cumulativeCounts[i] = 0;
        }
        _cumulativeCounts[i] = 0;
        updateRange();
    }

    private float scale(float value, float min, float max, float targetMin, float targetMax) {
        float unitValue = (value - min) / (max - min);
        return targetMin + (targetMax - targetMin) * unitValue;
    }

    private void initHistogram(float[] values){
        resetCounts();
        // region populate counts
        for (int offset = ImageData.OFFSET_R; offset < values.length; offset += ImageData.BYTES_PER_PIXEL) {
            float val = values[offset];
            int index = (int) scale(val, _min, _max, 0, _counts.length - 1);
            _counts[index]++;

        }
        // endregion

        // region prepare cumulative counts
        int total = 0;
        _cumulativeCounts[0] = 0;
        for (int i = 0; i < _counts.length; ) {
            total += _counts[i];
            i++;
            _cumulativeCounts[i] = total;
        }

        // endregion
    }

    private void initMaxMin(float[] values){
        _min = Float.POSITIVE_INFINITY;
        _max = Float.NEGATIVE_INFINITY;

        int offset;

        for (offset = ImageData.OFFSET_R; offset < values.length; offset += ImageData.BYTES_PER_PIXEL) {
            float val = values[offset];
            if(_min > val)
                _min = val;
            if(_max < val)
                _max = val;
        }
    }
    public void setConfiguration(Configuration conf){
        _conf = conf;
        if(_conf.sampleCount > 0) {
            if(_counts == null || _counts.length != _conf.sampleCount) {
                _counts = new int[_conf.sampleCount];
                _cumulativeCounts = new int[_conf.sampleCount + 1];
            }
        }
    }
    @Override
    public void performOperation() {
        ImageData image = getInput(0);
        assertSize(image);

        float[] values = image.getRawData();




        // do operation
        switch (_conf.mode)
        {
            case DivideWithWeight:
                initMaxMin(values);
                initHistogram(values);
                divideWithWeight();
                break;
            case Scale:
                initMaxMin(values);
                scaleAll();
                break;
            case DivideWithConstant:
                divideWithConstant();
                break;
        }
        copyRedToGreenBlue();
        // endregion
        sendOutput(_dataBuffer, 0);
    }

    private void divideWithConstant(){
        ImageData input = getInput(0);
        float[] values = input.getRawData();
        float[] target = _dataBuffer.getRawData();
        float constant = _conf.divideWeight;
        float low = _conf.minValue;
        float high  =_conf.maxValue;

        for (int offset = ImageData.OFFSET_R; offset < values.length; offset += ImageData.BYTES_PER_PIXEL) {
            target[offset] = values[offset] > constant ? high : low;

        }
    }

    private void scaleAll() {
        ImageData input = getInput(0);
        float[] values = input.getRawData();
        float[] target = _dataBuffer.getRawData();

        float low = _conf.minValue;
        float high  =_conf.maxValue;
        for (int offset = ImageData.OFFSET_R; offset < values.length; offset += ImageData.BYTES_PER_PIXEL) {
            target[offset] = scale(values[offset], _min, _max, low, high);

        }
    }
    private void divideWithWeight() {

        ImageData input = getInput(0);
        float[] values = input.getRawData();
        float[] target = _dataBuffer.getRawData();

        int end = _rangeStart + _rangeCount;
        int totalCount = _cumulativeCounts[end] - _cumulativeCounts[_rangeStart];
        float cumulativeValue = totalCount * _conf.divideWeight + _cumulativeCounts[_rangeStart];
        int divideIndex = Arrays.binarySearch(_cumulativeCounts, (int)cumulativeValue);
        if(divideIndex < 0) {
            divideIndex = -divideIndex - 1;
        }

        divideIndex--;

        float divideValue = scale(divideIndex, 0, _counts.length, _min, _max);


        int offset;

        for (offset = ImageData.OFFSET_R; offset < values.length; offset += ImageData.BYTES_PER_PIXEL) {
            float val = values[offset];
            target[offset] = val < divideValue ? _conf.minValue : _conf.maxValue;

        }
    }
    private void updateRange() {
        if(_conf.rangeStart < 0) {
            _rangeStart = 0;
        }
        else {
            _rangeStart =_conf.rangeStart;
        }

        if(_conf.rangeCount < 0){
            _rangeCount = _counts.length - _rangeStart;
        }
        else {
            _rangeCount = _conf.rangeCount;
        }

    }

    public static class Configuration {
        public Mode mode;
        public float divideWeight;
        public float maxValue, minValue;
        public int sampleCount;
        public int rangeStart;
        public int rangeCount;

        public static Configuration newDivideWithWeight(int sampleCount, float divideWeight){
            Configuration _this = new Configuration();
            _this.sampleCount = sampleCount;
            _this.mode = Mode.DivideWithWeight;
            _this.divideWeight = divideWeight;
            _this.maxValue = 1f;
            _this.minValue = 0f;
            _this.rangeCount = -1;
            _this.rangeStart = -1;
            return _this;
        }
        public static Configuration newScale(){
            Configuration _this = new Configuration();
            _this.mode = Mode.Scale;
            _this.maxValue = 1f;
            _this.minValue = 0f;
            _this.rangeCount = -1;
            _this.rangeStart = -1;
            return _this;
        }
        public static Configuration newConstantDivide(float constantValue){
            Configuration _this = new Configuration();
            _this.sampleCount = -1;
            _this.mode = Mode.DivideWithConstant;
            _this.maxValue = 1f;
            _this.minValue = 0f;
            _this.rangeStart = -1;
            _this.rangeCount = -1;
            _this.divideWeight = constantValue;
            return _this;
        }
        private Configuration(){

        }
        private Configuration(int sampleCount, Mode mode, float divideWeight) {
            this.sampleCount = sampleCount;
            this.mode = mode;
            this.divideWeight = divideWeight;
            this.maxValue = 1f;
            this.minValue = 0f;
            this.rangeCount = -1;
            this.rangeStart = -1;
        }
    }
    public enum Mode {
        DivideWithWeight,
        DivideWithConstant,
        Scale,


    }
}
