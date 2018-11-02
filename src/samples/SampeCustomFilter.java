package samples;

import com.mobge.simpleimageprocessing.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SampeCustomFilter {
    public final int width = 200, height = 200;



    public void createSequence(BlockIn filterIn, BlockOut filterOut) {
        CustomNegateFilter f = new CustomNegateFilter(width, height);
        filterIn.connectOutput(f);
        f.connectOutput(filterOut);

    }

    public SampeCustomFilter() throws IOException {

        BlockIn blockIn = new BlockIn();
        BlockOut blockOut = new BlockOut();
        createSequence(blockIn, blockOut);

        BufferedImage b = ImageIO.read(getClass().getResource("sample_image.jpeg"));
        ImageData input = new ImageData(b, width, height);

        blockOut.setListener(new BlockOut.Listener<ImageData>() {
            @Override
            public void resultIsReady(ImageData data) {
                try {
                    ImageIO.write(data.toBufferedImage(), "PNG", new File("sample_custom_filter_out_1.png"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        blockIn.setInput(input);

    }

    public static void main(String[] args) throws Exception{
        new SampeCustomFilter();
    }
}

class CustomNegateFilter extends BaseBufferedFilter {

    public CustomNegateFilter(int width, int height) {
        super(width, height);
    }

    @Override
    public void performOperation() {
        ImageData input = getInput(0);
        assertSize(input);

        // output buffer
        float[] outBuffer = getBuffer().getRawData();
        float[] inBuffer = input.getRawData();

        // perform the operation only on red channel
        for(int offset = ImageData.OFFSET_R; offset < inBuffer.length; offset += ImageData.BYTES_PER_PIXEL){
            outBuffer[offset] = 1.0f - inBuffer[offset];
        }

        // if this filter is connected to output copy red channel to others
        // to make the final image grayscale
        copyRedToGreenBlueIfOut();
        sendOutput(getBuffer(), 0);
    }
}