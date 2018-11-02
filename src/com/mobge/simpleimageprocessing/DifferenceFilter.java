package com.mobge.simpleimageprocessing;


public class DifferenceFilter extends BaseBufferedFilter{

    public final static int INPUT_LEFTHAND = 0, INPUT_RIGHTHAND = 1;
    public DifferenceFilter(Configuration configuration, int width, int height) {
        super(width, height);
        _configuration = configuration;
    }

    private Configuration _configuration;

    @Override
    public void performOperation() {
        ImageData d1 = getInput(INPUT_LEFTHAND);
        ImageData sub = getInput(INPUT_RIGHTHAND);

        float[] rD1 = d1.getRawData();
        float[] rSub = sub.getRawData();

        float[] out = _dataBuffer.getRawData();

        int w = _dataBuffer.getWidth();
        int h = _dataBuffer.getHeight();

        int bbp = ImageData.BYTES_PER_PIXEL;
        int length = w * h * bbp;

        assertSize(d1);
        assertSize(sub);

        int offset = ImageData.OFFSET_R;
        switch (_configuration.mode) {
            case ABSOLUTE_VALUE:
                for (; offset < length; offset+=bbp) {
                    out[offset] = Math.abs(rD1[offset] - rSub[offset]);
                }

                break;
                default:
                    for (; offset < length; offset+=bbp) {
                        out[offset] = rD1[offset] - rSub[offset];
                    }

                    break;
        }
        copyRedToGreenBlue();
        sendOutput(_dataBuffer, 0);
    }

    @Override
    public int numberOfInputs() {
        return 2;
    }
    public static class Configuration {
        public Mode mode = Mode.ABSOLUTE_VALUE;
        public Configuration(Mode mode) {
            this.mode = mode;
        }
    }
    public enum Mode {
        ABSOLUTE_VALUE,
        SUBSTRACTION
    }
}
