import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by Swayze on 9/6/2016.
 *
 * Intended to house a bunch of helpful functions for random number generation,
 * as well as image loading and manipulation
 */
public class CustomToolkit {

    public static String PARENT_DIRECTORY;              //  "<jar-directory>" or wherever the running file is located
    public static String IMAGE_FOLDER;                  //  "/images/"
    public static String IMAGE_DIRECTORY;               //  "<jar-directory>/images/"
    public static Color invisibleColor;

    public static BufferedImage endGameImage;

    //  Sets static parameters for the class, consisting of image directories and color use in removeTileBackground();
    public static void setParams(){
        PARENT_DIRECTORY = System.getProperty("user.dir");
        IMAGE_FOLDER = "\\images\\";
        IMAGE_DIRECTORY = PARENT_DIRECTORY + IMAGE_FOLDER;
        invisibleColor = new Color(200,50,240);                 //  The RGB of the imag that gets turned invisible during removeTileBackground();
    }

    //  Returns a random value between 0 (inclusive) and <max> (exclusive)
    public static int randomUpToExcluding(int max){
        return (int)(Math.random() * max);
    }

    //  Attempts to return the image by the name passed, if it exists;
    //  Looks for the image under "IMAGE_DIRECTORY/imageName"
    //  RETURNS the image passed or NULL, prints to console if image was not located;
    public static BufferedImage loadImage(String imageName){
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(IMAGE_DIRECTORY + imageName));
        }	catch(IOException e){
            System.out.println("CustomToolkit loadImage: Failed to find image by name of " + imageName + " using full path " + IMAGE_DIRECTORY + imageName);
        }
        return img;
    }

    //	Searches for all instances of that color in the image, then makes the pixels transparent.
    //  RETURNS the image passed or NULL, prints to console if image or InvisibleColor was NULL
    public static BufferedImage removeTileBackground(BufferedImage image){
        if(image!= null){
            if(invisibleColor != null) {
                for (int i = 0; i < image.getHeight(); i++) {
                    for (int j = 0; j < image.getWidth(); j++) {
                        if (image.getRGB(j, i) == invisibleColor.getRGB()) {
                            image.setRGB(j, i, 0x8F1C1C);
                        }
                    }
                }
            }   else    {   System.out.println("CustomToolkit removeTileBackground: Failed to modify image; invisible color is NULL");    }
        }   else    {   System.out.println("CustomToolkit removeTileBackground: Failed to modify image; image is NULL");    }
        return image;
    }

    //  Rotates the image passed by specified number of radians;
    //  RETURNS the image passed or NULL, prints to console if image passed was NULL
    public static BufferedImage rotateImage(BufferedImage img, double rotationInRadians){
        if(img != null) {
            AffineTransform tx = new AffineTransform();
            tx.rotate(rotationInRadians, img.getWidth() / 2, img.getHeight() / 2);
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
            img = op.filter(img, null);
        }   else    {
            System.out.println("CustomToolkit rotateImage: Failed to rotate image; image is NULL");
        }
        return img;
    }

    //  Adds together the elements of the two arrays at their respective indices.
    //  Prints to console if either of the two parameters was null;
    public static int[] fuseArrays(int[] a1, int[] a2){
        if(a1!=null) {
            if(a2!=null) {
                for (int i = 0; i < a1.length; i++) {
                    a1[i] = a1[i] + a2[i];
                }
            }   else    {   System.out.println("CustomToolkit fuseArrays: Failed to fuse arrays; second array passed was NULL");    }
        }   else    {   System.out.println("CustomToolkit fuseArrays: Failed to fuse arrays; first array passed was NULL"); }
        return a1;
    }

    //  Checks to see if the first number passed is between the other two numbers (inclusive); a no-brainer function, exists as a clarity shortcut.
    public static boolean isBetween(int target, int low, int high){
        return ((target >= low) && (target <= high));
    }

}
