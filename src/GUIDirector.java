import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Created by Swayze on 9/4/2016.
 *
 * Designed to be the go-to guy for the interface. Houses the frame, panel, and other interface things.
 *
 */
public class GUIDirector extends Thread{

    private CustomAction pressedUp;
    private CustomAction pressedDown;
    private CustomAction pressedLeft;
    private CustomAction pressedRight;

    private cWindowAdapter cwa;
    private cFrame frame;
    private cPanel canvasPanel;

    public static int WINDOW_WIDTH = 600;
    public static int WINDOW_HEIGHT = 600;

    public static int PANEL_WIDTH = 400;
    public static int PANEL_HEIGHT = 400;
    public static int DISPLAY_PADDING_WIDTH = 100;


    private int PAINT_DELAY;    /// Number of milliseconds the GUI waits before refreshing the screen.
    private boolean CONTINUE_PAINTING;

    private ArrayList<ArrayList> Drawables;
    private ArrayList<DrawableObject> background;
    private ArrayList<DrawableObject> foreground;

    public String actionToPerform;

    public static void setupStaticParams(){
        cPanel.setPaddingWidth(DISPLAY_PADDING_WIDTH);
    }

    public GUIDirector(){
        //  Initialize stuff,
        initializeParams();
        initializeFrame(WINDOW_WIDTH,WINDOW_HEIGHT);
        initializeDisplay(PANEL_WIDTH,PANEL_HEIGHT);
        //  Create Actions and assign keyboard presses
        createActions();
        //  Add the canvas to the panel,
        frame.add(canvasPanel);
        //  And begin.
        start();
    }

    private void initializeParams(){
        CustomToolkit.setParams();
        actionToPerform = " ";
        PAINT_DELAY = 50;
        CONTINUE_PAINTING = true;
    }

    private void setUpDirectories(){
        CustomToolkit.PARENT_DIRECTORY = "";
        CustomToolkit.IMAGE_DIRECTORY = "";
    }

    private void initializeFrame(int w, int h) {
        frame = new cFrame("TESTING");
        frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
        frame.setSize(w, h);
        cwa = new cWindowAdapter();
        frame.addWindowListener(cwa);
        frame.setVisible(true);
    }

    private void initializeDisplay(int w, int h) {
        canvasPanel = new cPanel(w,h);
        canvasPanel.loadToScreen(Drawables);
        canvasPanel.repaint();
    }

    //  Called when 'start()' is called.
    public void run(){
        try{
            while(CONTINUE_PAINTING) {
                Thread.sleep(PAINT_DELAY);
                canvasPanel.loadToScreen(Launch.gameObjects);
            }
        } catch(InterruptedException e) {
            System.out.println(Thread.currentThread().getName() + " was interrupted");
        }
    }

    //  Sets the time interval for refreshing graphics
    public void setPaintDelay(int delay){
        PAINT_DELAY = delay;
    }

    //  Toggles refreshing of graphics. Basically all images get frozen in place, but the game logic keeps ticking.
    public void togglePainting(){
        CONTINUE_PAINTING = !CONTINUE_PAINTING;
    }

    //Sets the size of the frame;   takes in two integers, width and height;
    public void setFrameSize(int width, int height) {
        frame.setSize(width, height);
    }

    //  Sets the size of the Canvas (panel) to that of its parent frame;
    public void matchCanvasSize() {
        setPanelSize(frame.getWidth(),frame.getHeight());
    }

    //Sets the size of the panel;   takes in two integers, width and height;
    public void setPanelSize(int width, int height) {
        canvasPanel.setSize(width, height);
    }

    //  Set background color of the panel;
    public void setBackgroundColor(Color color) {
        canvasPanel.setBackgroundColor(color);
    }

    //  Set background image of the panel;
    public void setBackgroundImage(BufferedImage image) {
        canvasPanel.setBackgroundImage(image);
    }

    private void createActions(){
        createAndAssignAction(pressedUp, "UP");
        createAndAssignAction(pressedDown, "DOWN");
        createAndAssignAction(pressedLeft, "LEFT");
        createAndAssignAction(pressedRight, "RIGHT");
    }

    private void createAndAssignAction(CustomAction action, String command){
        action = new CustomAction(command);
        action.setGUIDirectorReference(this);
        canvasPanel.getInputMap().put(KeyStroke.getKeyStroke(command), "pressed" + command);
        canvasPanel.getActionMap().put("pressed" + command, action);
    }

    public String getNextAction(){
        return actionToPerform;
    }

    public void replaceAction(String s){
        //System.out.println("Replacing next action of (" + actionToPerform + ") with (" + s + ")");
        actionToPerform = s;
    }

    public void clearAction(){
        actionToPerform = " ";
    }
}
