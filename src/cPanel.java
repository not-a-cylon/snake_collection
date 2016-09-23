/**
 * Created by Swayze on 9/4/2016.
 *
 * The class is basically a canvas that's housed within a Frame.
 * You can set its bg color, bg image, size, etc
 * Stuff s drawn to a buffer called 'canvas' through the method 'loadToScreen'
 * Canvas is then drawn into a panel. The buffer is used in order to prevent flickering graphics.
 *
 */
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JPanel;

public class cPanel extends JPanel {

    private BufferedImage canvas;			//	The buffer that everything gets drawn on before going onscreen. This prevents choppy graphics.
    private Graphics2D canvasGraphics;		//	The buffer's Graphics object that draws everything.

    private Color backgroundColor;          //  default background color for the panel;
    private BufferedImage backgroundImage;   //  image for the background. Gets defaulted to a plain rectangle the color of backgroundColor;
    private Graphics2D backgroundGraphics;

    private int paintCount = 0;
    private boolean DEBUG = false;

    //  PANEL dimensions include the width of the display (where the game is drawn) and PADDING_WIDTH (where the HUD is drawn)
    public int PANEL_WIDTH;
    public int PANEL_HEIGHT;

    public int PADDING_WIDTH = 20;

    public int DISPLAY_WIDTH;
    public int DISPLAY_HEIGHT;





    public cPanel(){
        this(400,400);
    }

    public cPanel(int w, int h){
        setSize(w,h);   //  Sets size of the panel

        canvas = new BufferedImage(getWidth(),getHeight(), BufferedImage.TYPE_INT_ARGB);
        canvasGraphics = (Graphics2D)canvas.getGraphics();

        backgroundColor = Color.BLACK;  //default BG color, until overridden;
        backgroundImage = new BufferedImage(getWidth() - 2*PADDING_WIDTH,getHeight()  - 2*PADDING_WIDTH, BufferedImage.TYPE_INT_ARGB);
        backgroundGraphics = (Graphics2D)backgroundImage.getGraphics();

        setBackgroundToDefault();
    }

    //	This magically gets called at random times throughout the panel's existence. It's a black box that works.
    //		It's important that the super's paintComponent class is called first, for some reason.
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(canvas, PADDING_WIDTH, PADDING_WIDTH, this);	// this is the important bit that draws the buffer onto the screen.
    }

    public void loadImageToScreen(BufferedImage image){
        loadImageToScreen(image,0,0);
        //canvasGraphics.drawImage(image,0,0,this);
        repaint();
    }

    //  Paints the image over everything else still in memory; this gets erased when refreshed, though;
    public void loadImageToScreen(BufferedImage image, int x, int y){
        canvasGraphics.drawImage(image,x,y,this);
        repaint();
    }

    //  Scans through the passed list of DrawableObjects and draws them onto the buffer using their location and image.
    //  Afterwards, the buffer is drawn to the screen using the repaint() method.
    public void loadListToScreen(ArrayList<DrawableObject> objects){
        if(!(objects == null) && !(objects.size()==0)){
            int listSize = objects.size();
            for(int i = 0; i < listSize; i++) {
                DrawableObject obj = objects.get(i);
                canvasGraphics.drawImage(obj.getCurrentIcon(), obj.getX(), obj.getY(), this);
            }
        }
        //repaint();
    }

    //  First and foremost, draws its default backgroundImage on the buffer
    //  Then draws every object in the collection passed by first splitting it into individual lists, then calling loadListToScreen() on each one;
    public void loadToScreen(ArrayList<ArrayList> listsToDraw){
        canvasGraphics.drawImage(backgroundImage, 0, 0, this);
        if(!(listsToDraw == null) && !(listsToDraw.size()==0)){
            int max = listsToDraw.size();
            for(int i = 0; i < max; i++) {
                loadListToScreen(listsToDraw.get(i));
            }
        }
        repaint();
    }

    //  Sets the color of the background to the color passed, then repaints the background.
    public void setBackgroundColor(Color color){
        backgroundColor = color;
        setBackgroundToDefault();
    }

    //  Repaints the background image to a plain rectangle of the size of the panel and the default color.
    public void setBackgroundToDefault(){
        backgroundGraphics.setColor(backgroundColor);
        backgroundGraphics.fillRect(0,0,getWidth(),getHeight());
    }

    //  Sets the background image to a custom one.
    public void setBackgroundImage(BufferedImage newBG){
        backgroundImage = newBG;
        backgroundGraphics = (Graphics2D)backgroundImage.getGraphics();
    }

    public void setPaddingWidth(int pad){
        PADDING_WIDTH = pad;
    }

}
