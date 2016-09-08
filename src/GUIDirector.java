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

    private cWindowAdapter cwa;
    private cFrame frame;
    private cPanel panel;

    private int FRAME_WIDTH;
    private int FRAME_HEIGHT;

    private int PAINT_DELAY;    /// Number of milliseconds the GUI waits before refreshing the screen.
    private boolean CONTINUE_PAINTING;

    private ArrayList<ArrayList> Drawables;
    private ArrayList<DrawableObject> background;
    private ArrayList<DrawableObject> foreground;

    private CustomAction pressedUp;
    private CustomAction pressedDown;
    private CustomAction pressedLeft;
    private CustomAction pressedRight;

    public String actionToPerform;

    public GUIDirector() {
        this(400,400,400,400);
    }

    public static void setupStaticParams(){}

    public GUIDirector(int frameWidth, int frameHeight, int panelWidth, int panelHeight) {

        //  Initialize stuff,
        initializeParams();
        initializeFrame(frameWidth,frameHeight);
        initializePanel(panelWidth,panelHeight);
        //  Create Actions and assign keyboard presses
        createActions();

        //  Add the canvas to the panel,
        frame.add(panel);
        //  And begin.
        start();
    }

    private void initializeParams(){
        CustomToolkit.setParams();
        actionToPerform = " ";
        background = new ArrayList<DrawableObject>();
        foreground = new ArrayList<DrawableObject>();
        Drawables = new ArrayList<ArrayList>();
        Drawables.add(background);
        Drawables.add(foreground);
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

    private void initializePanel(int w, int h) {
        panel = new cPanel(w,h);
        panel.loadToScreen(Drawables);
        panel.repaint();
    }

    //  Called when 'start()' is called.
    public void run(){
        try{
            while(CONTINUE_PAINTING) {
                Thread.sleep(PAINT_DELAY);
                panel.loadToScreen(Drawables);
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
        panel.setSize(width, height);
    }

    //  Set background color of the panel;
    public void setBackgroundColor(Color color) {
        panel.setBackgroundColor(color);
    }

    //  Set background image of the panel;
    public void setBackgroundImage(BufferedImage image) {
        panel.setBackgroundImage(image);
    }

    //  Adds a DrawableObject to the drawables list, which is then drawn onscreen by the panel at every cycle.
    public void addToBackground(DrawableObject obj){
        if(!background.contains(obj)){
            background.add(obj);
        }
    }

    public void addToForeground(DrawableObject obj){
        if(!foreground.contains(obj)){
            foreground.add(obj);
        }
    }

    //  Removes a DrawableObject from the drawables list, which is then drawn onscreen by the panel at every cycle.
    public void removeFromScreen(DrawableObject obj){
        int max = Drawables.size();
        for(int i = 0; i < max; i++){
            ArrayList<DrawableObject> list = Drawables.get(0);
            if(list.contains(obj)){
                list.remove(obj);
            }
        }
    }

    //  Removes all Drawable objects from the screen;
    public void clearDrawables(){
        int max = Drawables.size();
        for(int i = 0; i < max; i++){
            ArrayList<DrawableObject> list = Drawables.get(i);
            list.clear();
        }
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
        panel.getInputMap().put(KeyStroke.getKeyStroke(command), "pressed" + command);
        panel.getActionMap().put("pressed" + command, action);
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
