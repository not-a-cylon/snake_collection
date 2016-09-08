import java.awt.image.BufferedImage;

/**
 * Created by Swayze on 9/5/2016.
 */
public class Treat extends DrawableObject{

    //  DEFAULT STATIC PARAMETERS
    public static int DEFAULT_IMAGE_WIDTH;
    public static int DEFAULT_IMAGE_HEIGHT;
    public static String DEFAULT_IMAGE_PATH;

    public static BufferedImage DEFAULT_BUFFERED_IMAGE;

    //  Sets up the default static parameters for all objects to share; called early on by the processor
    public static void setupStaticImageParams(){
        DEFAULT_BUFFERED_IMAGE = CustomToolkit.loadImage("\\Treat\\default.png");
        DEFAULT_IMAGE_WIDTH = 20;
        DEFAULT_IMAGE_HEIGHT = 20;
        if(DEFAULT_BUFFERED_IMAGE != null){
            DEFAULT_BUFFERED_IMAGE = CustomToolkit.removeTileBackground(DEFAULT_BUFFERED_IMAGE);
            DEFAULT_IMAGE_WIDTH = DEFAULT_BUFFERED_IMAGE.getWidth();
            DEFAULT_IMAGE_HEIGHT = DEFAULT_BUFFERED_IMAGE.getHeight();
        }   else    {
            System.out.println("Treat was not able to load image from setupStaticImageParams");
        }
    }

    public Treat(){
        this(DEFAULT_IMAGE_WIDTH,DEFAULT_IMAGE_HEIGHT);
        setXYLocation(0,0);
    }

    public Treat(int w, int h){
        super(w,h);
        setSize(DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT);
        setIcon(DEFAULT_BUFFERED_IMAGE);
        //setGridLocation(gridX, gridY);
    }

}
