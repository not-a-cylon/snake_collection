import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Created by Swayze on 9/4/2016.
 *
 * Designed to be the go-to guy for the interface. Houses the frame, panel, and other interface things.
 *
 */
public class GUIDirector extends Thread implements ActionListener{

    private static Launch launchReference;          //  A reference to the processor

    private CustomAction pressedUp;                 //  Keyboard action stuff
    private CustomAction pressedDown;
    private CustomAction pressedLeft;
    private CustomAction pressedRight;
    private CustomAction pressedSpacebar;

    private cFrame frame;                           //  Main window
    private cWindowAdapter cwa;
    public static int WINDOW_WIDTH = 440;           //  Width and height of the window
    public static int WINDOW_HEIGHT = 540;

    private JPanel startPanel;                      //  Temporary panel that pops up when the game is loaded.
    private JTextArea welcomeMessage;               //  Text that gets displayed when the game is loaded;
    private JTextArea filler;                       //  Literally exists to provide makeshift padding;
    private JTextArea filler2;                       //  Literally exists to provide makeshift padding;
    private JButton startButton;                    //  Starts the game when pressed;
    private JTextArea graphicsDisclaimer;

    private JPanel parentPanel;                     //  Panel that fills the entirety of the Frame; exists for convenience.

    private JPanel sidePanel;                       //  Panel that goes on the bottom of the screen; displays the score;
    public static int SIDEPANEL_WIDTH = 400;
    public static int SIDEPANEL_HEIGHT = 50;

    private cPanel displayPanel;                    //  The display where game objects and backgrounds get drawn;
                                                    //  Dimensions of the displayPanel include the blank padding as well as desired dimensions of the 'canvas', where images get drawn
    public static int CANVAS_WIDTH = 400;          //  Width and height of the canvas inside the displayPanel
    public static int CANVAS_HEIGHT = 400;
    public static int DISPLAY_PADDING_WIDTH = 20;   //  blank 'padding' between the edges of the Canvas and painted images

    private JTextArea scoreLabel;                   //  Text that says "Score"
    private JTextArea scoreDisplay;                 //  Text that displays the score; updated by the processor as needed;

    public static BufferedImage endGameImage;   //  The image that gets drawn over everything once the game is over.
    public boolean CONTINUE_PAINTING;           //  This controls the main loop called on at start() or run();

    public boolean CONTINUE_REFRESHING_WORLD = false;   //  If set to true, the GUIDirector will keep painting game objects onscreen

    public static int SCORE_LENGTH = 6;     //  Determines the number of shoen digits in the score; if actual score is shorter, fills the difference with zeroes.

    private int PAINT_DELAY;    /// Number of milliseconds the GUI waits before refreshing the screen.
    private ArrayList<ArrayList> Drawables;
    private ArrayList<DrawableObject> background;
    private ArrayList<DrawableObject> foreground;

    public String actionToPerform = " ";

    public GUIDirector(Launch launchRef){
        setLaunchReference(launchRef);

        //  Initialize stuff,
        initializeParams();
        initializeFrame();
        initializePanels();

        //  Create Actions and assign keyboard presses
        createActions();

        //  Load the 'Welcome' screen
        loadStartPanel();

        //  Display the UI
        frame.add(parentPanel);
        frame.pack();
    }

    public void setLaunchReference(Launch launch){
        launchReference = launch;
    }

    private void initializeParams(){
        CustomToolkit.setParams();
        actionToPerform = " ";
        PAINT_DELAY = 50;
        CONTINUE_PAINTING = true;
        CONTINUE_REFRESHING_WORLD = true;
    }

    private void setUpDirectories(){
        CustomToolkit.PARENT_DIRECTORY = "";
        CustomToolkit.IMAGE_DIRECTORY = "";
    }

    private void initializeFrame() {
        frame = new cFrame("SNAKE SNAKE SNAKE");
        //frame.setPreferredSize(new Dimension(WINDOW_WIDTH,WINDOW_HEIGHT));
        frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
        cwa = new cWindowAdapter();
        frame.addWindowListener(cwa);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    private void initializePanels(){
        //  Parent panel that fills the frame
        parentPanel = new JPanel();
        parentPanel.setPreferredSize(new Dimension(WINDOW_WIDTH,WINDOW_HEIGHT));
        parentPanel.setBackground(Color.BLACK);
        parentPanel.repaint();

        //  The display that stuff gets painted on
        displayPanel = new cPanel(CANVAS_WIDTH + 2*DISPLAY_PADDING_WIDTH,CANVAS_HEIGHT + 2*DISPLAY_PADDING_WIDTH);
        displayPanel.setPreferredSize(new Dimension(CANVAS_WIDTH + 2*DISPLAY_PADDING_WIDTH,CANVAS_HEIGHT + 2*DISPLAY_PADDING_WIDTH));
        displayPanel.setPaddingWidth(DISPLAY_PADDING_WIDTH);
        displayPanel.setBackground(Color.BLACK);
        displayPanel.loadToScreen(Drawables);
        displayPanel.repaint();

        Font font = new Font("Verdana", Font.BOLD, 20);

        //  Text field that says "SCORE";
        scoreLabel = new JTextArea("SCORE: ");
        scoreLabel.setBackground(Color.BLACK);
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setFont(font);
        scoreLabel.setEditable(false);

        //  Text field that displays the score throughout the game; its value is changed through gui.displayScore(int);
        scoreDisplay = new JTextArea("0000000");
        scoreDisplay.setPreferredSize(new Dimension(100,30));
        scoreDisplay.setFont(font);
        scoreDisplay.setBackground(Color.BLACK);
        scoreDisplay.setForeground(Color.WHITE);
        scoreDisplay.setEditable(false);

        //  Gets placed inside ParentPanel and below canvasPanel
        sidePanel = new JPanel();
        sidePanel.setPreferredSize(new Dimension(SIDEPANEL_WIDTH + 2*DISPLAY_PADDING_WIDTH,SIDEPANEL_HEIGHT));
        sidePanel.setOpaque(true);
        sidePanel.setBackground(Color.BLACK);
        sidePanel.add(scoreLabel);
        sidePanel.add(scoreDisplay);
        sidePanel.setAlignmentY(Component.CENTER_ALIGNMENT);

        //  WELCOME text;
        welcomeMessage = new JTextArea("THIS IS SNAKE");
        welcomeMessage.setBackground(Color.BLACK);
        welcomeMessage.setForeground(Color.WHITE);
        welcomeMessage.setFont(font);
        welcomeMessage.setEditable(false);

        //  This TextArea literally exists to fill up space in the panel;
        filler = new JTextArea("                            ");
        filler.setBackground(Color.BLACK);
        filler.setFont(font);
        filler.setEditable(false);

        //  This TextArea literally exists to fill up space in the panel;
        filler2 = new JTextArea("                            ");
        filler2.setBackground(Color.BLACK);
        filler2.setFont(font);
        filler2.setEditable(false);

        graphicsDisclaimer = new JTextArea();
        graphicsDisclaimer.setText(" ");
        graphicsDisclaimer.setPreferredSize(new Dimension(250,100));
        graphicsDisclaimer.setFont(new Font("Verdana", Font.BOLD, 10));
        graphicsDisclaimer.setLineWrap(true);
        graphicsDisclaimer.setForeground(Color.WHITE);
        graphicsDisclaimer.setBackground(Color.BLACK);
        graphicsDisclaimer.setEditable(false);

        //  Clicked to start the game;  the GUIDirector is used as the ActionListener
        startButton = new JButton("START");
        startButton.setBackground(Color.BLACK);
        startButton.setForeground(Color.WHITE);
        startButton.setFont(font);
        startButton.setActionCommand("START");
        startButton.addActionListener(this);

        //  "start" Panel that gets displayed at the start of the game, and disappears once game starts
        startPanel = new JPanel();
        startPanel.setBackground(Color.BLACK);
        startPanel.setPreferredSize(new Dimension(250,200));

        //  Add the components;
        startPanel.add(welcomeMessage);
        startPanel.add(filler);
        startPanel.add(startButton);
        startPanel.add(filler2);
        startPanel.add(graphicsDisclaimer);

    }

    //  Adds gameplay panels to the screen, in the form of the canvas and score display.
    //  Combination of FlowLayout and limited size of the frame make the components stack from top to bottom.
    private void displayGameScreen(){
        parentPanel.removeAll();
        FlowLayout fl = new FlowLayout();
        fl.setHgap(0);
        fl.setVgap(0);
        parentPanel.setLayout(fl);
        parentPanel.add(displayPanel);
        parentPanel.add(sidePanel);
        parentPanel.repaint();
    }

    private void startGame(){
        CONTINUE_REFRESHING_WORLD = true;
        launchReference.GUI_IS_READY = true;    //  Tell the processor that the game HAS begun
        start();
    }

    //  Called when 'start()' is called. This contains the main loop, which mainly tells the canvasPanel to refresh;
    public void run(){
        try{
            while(CONTINUE_PAINTING) {          //  This is always set to true;
                while(CONTINUE_REFRESHING_WORLD) {       //  while CONTINUE_REFRESHING_WORLD is set to true, all objects in the world will continue to be repainted;
                                                            //  Set to false when the game ends, and reset to true when restarted;
                    Thread.sleep(PAINT_DELAY);
                    displayPanel.loadToScreen(Launch.gameObjects);
                }                               //  Once the game has ended, keep sending the endGameImage to the canvasPanel to paint over everything
                displayPanel.loadImageToScreen(endGameImage,100,100);
                Thread.sleep(100);
            }
        } catch(InterruptedException e) {
            System.out.println(Thread.currentThread().getName() + " was interrupted");
        }
    }

    //  Replaces contents of the parentPanel with the startPanel
    private void loadStartPanel(){
        parentPanel.removeAll();
        parentPanel.setLayout(new GridBagLayout());
        parentPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
        parentPanel.add(startPanel);
    }

    //  Sets the time interval for refreshing graphics
    public void setPaintDelay(int delay){
        PAINT_DELAY = delay;
    }

    //  Changes the score inside the scoreDisplay to the one passed;
    //  If the length of the 'score' String is shorter than SCORE_LENGTH, add zeros to the front until it fits.
    public void displayScore(int score){
        String scoreString = "" + score;
        while(scoreString.length() < SCORE_LENGTH){
            scoreString = "0" + scoreString;
        }
        if(scoreDisplay!=null) {
            scoreDisplay.replaceRange(scoreString, 0, scoreDisplay.getText().length());
        }   else    {
            System.out.println("scoreDisplay is null;");
        }
    }

    //  Set background image of the panel;
    public void setBackgroundImage(BufferedImage image) {
        displayPanel.setBackgroundImage(image);
    }


    //  --------    RELATED TO THE JBUTTON BEING PRESSED ------------
    public void actionPerformed(ActionEvent e){
        //  If the START button was pressed, hide the START menu, Display the canvas and score, and start the game.
        if ("START".equals(e.getActionCommand())){
            displayGameScreen();
            startGame();
        }
    }

    //  ---------------------   KEYBOARD ACTIONS    -----------
    //  Creates actions that drive keyboard prompts
    //  createActions and createAndAssignAction exist for convenience.
    //  When a key is pressed, a value representing the action is stored in the actionToPerform String, which is read by the processor for every logic step.
    private void createActions(){
        createAndAssignAction("UP");
        createAndAssignAction("DOWN");
        createAndAssignAction("LEFT");
        createAndAssignAction("RIGHT");
        createAndAssignAction("SPACE");

    }

    private void createAndAssignAction(String command){
        CustomAction customAction = new CustomAction(command);
        customAction.setGUIDirectorReference(this);
        parentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(command), "pressed" + command);
        parentPanel.getActionMap().put("pressed" + command, customAction);
    }

    //  Called by the Processor; returns the next action that the user wishes to preform, in the form of a String
    public String getNextAction(){
        return actionToPerform;
    }

    //  Called by the CustomAction when it's activated
    public void replaceAction(String s){
        actionToPerform = s;
    }

    //  Clears the value of intended action
    public void clearAction(){
        actionToPerform = " ";
    }

    //  Changes the wording of the Welcome screen to note that the iamges didn't load properly;
    public void enableGraphicsWarning(){
        graphicsDisclaimer.setText("        LOOKS LIKE SOME IMAGES DIDN'T \n" +
                "            LOAD PROPERLY. MAKE SURE \n" +
                "           IMAGE FOLDER IS IN THE SAME \n" +
                "            DIRECTORY AS THE .JAR/.EXE");
    }
}
