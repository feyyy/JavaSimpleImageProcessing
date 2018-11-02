package com.mobge.simpleimageprocessing;


public class CurvatureFilter extends BaseBufferedFilter{
    public CurvatureFilter(int width, int height) {
        super(width, height);
    }


    @Override
    public void performOperation() {
        ImageData image = getInput(0);
        super.assertSize(image);

        int w = image.getWidth();
        int h = image.getHeight();

        final float[] source = image.getRawData();
        final float[] target = _dataBuffer.getRawData();

        final int bbp = ImageData.BYTES_PER_PIXEL;
        final int stride = bbp * w;

        int offset = ImageData.OFFSET_R;
        int end = source.length - stride;
        int lineEnd = stride - bbp;
        offset += stride;
        for(; offset < end; offset += stride) {
            for(int x = bbp; x < lineEnd; x += bbp) {
                int iCenter = offset + x;
                float value = source[iCenter];

                float dif = 0;
                int count = 0;
                float next;

                next = source[iCenter + stride];
                if(next != 0){
                    count++;
                    dif += next;
                }
                next = source[iCenter - stride];
                if(next != 0){
                    count++;
                    dif += next;
                }
                next = source[iCenter + bbp];
                if(next != 0){
                    count++;
                    dif += next;
                }
                next = source[iCenter - bbp];
                if(next != 0){
                    count++;
                    dif += next;
                }

                float result;
                if(count > 0){
                    result = dif / count - value + 0.5f;
                }
                else{
                    result = value;
                }
                target[iCenter] = result;
            }
        }

        copyRedToGreenBlueIfOut();
        sendOutput(_dataBuffer, 0);
    }

}
