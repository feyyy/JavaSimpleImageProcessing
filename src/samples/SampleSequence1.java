package samples;

import com.mobge.simpleimageprocessing.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SampleSequence1 {

    public final int width = 200, height = 200;

    public void createSequence(BlockIn filterIn, BlockOut filterOut) {
        BoxBlurFilter.Configuration confBlur = new BoxBlurFilter.Configuration(3, 1);
        BoxBlurFilter filterBlur = new BoxBlurFilter(width, height).setConfiguration(confBlur);

        DifferenceFilter.Configuration confDiff = new DifferenceFilter.Configuration(DifferenceFilter.Mode.ABSOLUTE_VALUE);
        DifferenceFilter differenceFilter = new DifferenceFilter(
                confDiff, width, height);

        DivideFilter contrastFilter = new DivideFilter(width, height);
        contrastFilter.setConfiguration(DivideFilter.Configuration.newContrast());


        filterIn.connectOutput(0, differenceFilter, DifferenceFilter.INPUT_LEFTHAND);
        filterIn.connectOutput(filterBlur);
        filterBlur.connectOutput(0, differenceFilter, DifferenceFilter.INPUT_RIGHTHAND);
        differenceFilter.connectOutput(contrastFilter);
        contrastFilter.connectOutput(filterOut);

    }

    public SampleSequence1() throws IOException{

        BlockIn blockIn = new BlockIn();
        BlockOut blockOut = new BlockOut();
        createSequence(blockIn, blockOut);

        BufferedImage b = ImageIO.read(getClass().getResource("sample_image.jpeg"));
        ImageData input = new ImageData(b, width, height);

        blockOut.setListener(new BlockOut.Listener<ImageData>() {
            @Override
            public void resultIsReady(ImageData data) {
                try {
                    ImageIO.write(data.toBufferedImage(), "PNG", new File("sample_sequence_out_1.png"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        blockIn.setInput(input);

    }

    public static void main(String[] args) throws Exception{
        new SampleSequence1();
    }
}
