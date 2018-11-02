# JavaSimpleImageProcessing
Image processing filters for java. All filters assumes the input image is grayscale. All filters has a nested "Configuration" class. To use a filter Configuration class of a filter must be created and given to the filter via its constructor or its setConfiguration method.

Filters are designed to be connected to each other to create a filter sequence.

All filters uses a floating point number for each color channel of a pixel.

# Using code
Copy "com" folder under src into your src folder

# Sample Usage
Lets create a box blur and take difference between blurred image and original image. Finaly increase the contrast to make differences visible with human eye.
```
    ______
   |      |
   |  In  |
   |______|
      / \
 ____/_  \
|      |  |
| blur |  |
|______|  |
   \     /
   _\___/_
  |       |
  |  dif  |
  |_______|
      |
  ____|_____
 |   Fix    |
 | Contrast |
 |__________|
      |
    __|___
   |      |
   |  Out |
   |______|
```
```java
// create necessary filters to use
int width = <image width>, height = <image height>;

BlockIn filterIn = new BlockIn(); // feed input from here
BlockOut filterOut = new BlockOut(); // listen output from here

BoxBlurFilter.Configuration confBlur = new BoxBlurFilter.Configuration(3, 2);
BoxBlurFilter filterBlur = new BoxBlurFilter(width, height).setConfiguration(confBlur);

DifferenceFilter.Configuration confDiff = new DifferenceFilter.Configuration(DifferenceFilter.Mode.ABSOLUTE_VALUE);
DifferenceFilter differenceFilter = new DifferenceFilter(
                confDiff, width, height);
                
DivideFilter contrastFilter = new DivideFilter(width, height);
contrastFilter.setConfiguration(DivideFilter.Configuration.newContrast());

// now connect the filters to each other

filterIn.connectOutput(0, differenceFilter, DifferenceFilter.INPUT_LEFTHAND);
filterIn.connectOutput(filterBlur);
filterBlur.connectOutput(0, differenceFilter, DifferenceFilter.INPUT_RIGHTHAND);
 differenceFilter.connectOutput(contrastFilter);
contrastFilter.connectOutput(filterOut);


// since we created our sequence, we can use this sequence any number of times
ImageData anInput = new ImageData(new File("<input path>"));
// handle result image
filterOut.setListener(new BlockOut.Listener<ImageData>() {
   @Override
   public void resultIsReady(ImageData result) {
     ImageIO.write(result.toBufferedImage(), "PNG", new File("<outputPath>" + ".png"));
   }
});
// feed input image
blockIn.setInput(anInput);
```
