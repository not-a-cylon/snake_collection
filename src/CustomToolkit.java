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
 * Intended to house a bunch of helpful methods for random number generation,
 * as well as image loading and manipulation
 */
public class CustomToolkit {

    public static String PARENT_DIRECTORY;
    public static String IMAGE_FOLDER;
    public static String IMAGE_DIRECTORY;
    public static Color invisibleColor;

    public static void setParams(){
        PARENT_DIRECTORY = System.getProperty("user.dir");
        IMAGE_FOLDER = "\\images\\";
        IMAGE_DIRECTORY = PARENT_DIRECTORY + IMAGE_FOLDER;
        invisibleColor = new Color(200,50,240);

        ///System.out.println(PARENT_DIRECTORY);
        ///System.out.println(IMAGE_FOLDER);
        //System.out.println(IMAGE_DIRECTORY);
    }

    public CustomToolkit(){

    }

    //  Returns a random value between 0 (inclusive) and <max> (exclusive)
    public static int randomUpToExcluding(int max){
        return (int)(Math.random() * max);
    }

    public static BufferedImage loadImage(String imageName){
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(IMAGE_DIRECTORY + imageName));
        }	catch(IOException e){
            System.out.println("Failed to find image by name of " + imageName + " at " + IMAGE_DIRECTORY + imageName);
        }
        return img;
    }

    //	Searches for all instances of that color in the image, then makes the pixels transparent.
    public static BufferedImage removeTileBackground(BufferedImage image){
        if(image!= null && invisibleColor != null){
            for(int i = 0; i < image.getHeight(); i++) {
                for(int j = 0; j < image.getWidth(); j++) {
                    if(image.getRGB(j, i) == invisibleColor.getRGB()) {
                        image.setRGB(j, i, 0x8F1C1C);
                    }
                }
            }
        }
        return image;
    }

    public static BufferedImage rotateImage(BufferedImage img, double rotationInRadians){
        AffineTransform tx = new AffineTransform();
        tx.rotate(rotationInRadians, img.getWidth() / 2, img.getHeight() / 2);
        AffineTransformOp op = new AffineTransformOp(tx,AffineTransformOp.TYPE_BILINEAR);
        img = op.filter(img, null);
        return img;
    }

    //  Adds together the elements of the two arrays at their respective indices.
    public static int[] fuseArrays(int[] a1, int[] a2){
        for(int i = 0; i < a1.length; i++){
            a1[i] = a1[i] + a2[i];
        }
        return a1;
    }

}
