import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by Swayze on 9/4/2016.

    This is intended to be a base class for a Drawable Object, containing only basic size and location methods.

 */
public class DrawableObject {

    protected static BufferedImage staticDefaultIcon;
    protected BufferedImage defaultIcon;
    protected Graphics2D graphics;

    //  Image dimensions that the object defaults to.
    public static int DEFAULT_IMAGE_WIDTH = 100;
    public static int DEFAULT_IMAGE_HEIGHT = 100;
    public static String DEFAULT_IMAGE_DIRECTORY;

    //  Current width and height og the image
    protected int IMAGE_WIDTH;
    protected int IMAGE_HEIGHT;

    protected int x;
    protected int y;

    protected int[] gridLocation;
    protected int[] nextGridLocation;

    public static void setStringTest(String s){
        DEFAULT_IMAGE_DIRECTORY = s;
    }

    //  Needs to get called by the game processor and rewritten for every different type of object. This sets up a bunch of static variables for instances of this object to refer to,
    //      like the default image directory, BufferedImage, and default image size;
    public static void initializeStaticParams(){
        //DEFAULT_IMAGE_DIRECTORY = "DrawableObject/default.png";
        //staticDefaultIcon = CustomToolkit.loadImage(DEFAULT_IMAGE_DIRECTORY);
        //DEFAULT_IMAGE_WIDTH = 100;
        //DEFAULT_IMAGE_HEIGHT = 100;
    }

    public DrawableObject(){
        this(50,50);
        setXYLocation(0,0);
        generateIcon();
    }

    public DrawableObject(int w, int h){
        setSize(w,h);
        gridLocation = new int[2];
    }

    public void calculateStep() {}

    public void step(){}

    public void updateObjectImage(){}

    private void generateIcon(){
        defaultIcon = new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_INT_ARGB);
        graphics = (Graphics2D)defaultIcon.getGraphics();
        graphics.setColor(Color.GRAY);
        graphics.fillRect(0, 0, getWidth(), getHeight());
    }

    public void setIcon(BufferedImage image){
        defaultIcon = image;
    }

    public BufferedImage getCurrentIcon(){
        return defaultIcon;
    }

    //  Standard setters and getters for the width and height of the object;
    public void setSize(int W, int H){
        setWidth(W);
        setHeight(H);
    }


    public void setGridLocation(int x, int y){
        gridLocation[0] = x;
        gridLocation[1] = y;
        setXYLocation(x*IMAGE_WIDTH, y*IMAGE_HEIGHT);
    }

    public int[] getGridLocation(){
        return gridLocation;
    }

    //  This might cause problems
    public int[] getNextGridLocation(){
        return gridLocation;
    }

    public boolean sameGridLocationAs(int[] loc){
        for(int i = 0; i < loc.length; i++){
            if(loc[i] != gridLocation[i]){
                return false;
            }
        }
        return true;
    }

    public void setWidth(int w){
        IMAGE_WIDTH = w;
    }

    public void setHeight(int h){
        IMAGE_HEIGHT = h;
    }

    public int getWidth(){
        return IMAGE_WIDTH;
    }

    public int getHeight(){
        return IMAGE_HEIGHT;
    }

    //  Standard setters and getters for the x and y coordinates of the object;
    public void setXYLocation(int X, int Y){
        setX(X);
        setY(Y);
    }

    public void setX(int X){
        x = X;
    }

    public void setY(int Y){
        y = Y;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

}
