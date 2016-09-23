import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Created by Swayze on 9/4/2016.
 * The brain of the program, subject to change drastically based on whatever program the panels and frames end up being used for.
 */
public class Launch {

    private GUIDirector gui;

    //  --------------------------------------------        CONSOLE VARS    -   change these to change outlook of the game. Intended to be intuitive.
    private int gridSize = 20;               //  The number of cells, in one direction, that the world should be split into.
    private int DEFAULT_LENGTH = 8;          //  Starting length of the snake.
    private char DEFAULT_DIRECTION = 'r';    //  Starting direction the snake faces.

    //  --------------------------------------------        INTERNAL VARS    -   vars used by the system.
    public static ArrayList<ArrayList> gameObjects;         //  A collection of the below lists, for brevity's sake.
    public static ArrayList<DrawableObject> background;     //  Images to be drawn in the background
    public static ArrayList<DrawableObject> inBetween;      //  Images to be drawn in-between
    public static ArrayList<DrawableObject> foreground;     //  Images to be drawn in the foreground

    //  --------------------------------------------        INTERFACE DIMENSIONS

    private int cellWidth;                              //  width and height of the cell, CALCULATED based on gridSize;
    private int cellHeight;

    private int PAINT_DELAY = 10;                       //  delay between painting of the frames by the GUI

    private int DEFAULT_LOGIC_DELAY = 100;
    private int LOGIC_DELAY;                             //  delay between steps in game logic (if it's even needed)

    private int GAME_SCORE = 0;

    private boolean KEEP_PROCESSING = true;             //  Boolean that runs the game; if set to false, processes stop.
    public boolean GUI_IS_READY = false;                //  Needs to be true for processor to step();   gets set to true when GUI is ready for gameplay, ie START is pressed;
    private boolean REFRESH_WORLD = false;              //  Usually false; gets check at every step() and triggers refreshWorld() if true;
    private boolean GRAPHICS_FAILED;                    //  Gets set to 'true' if there's any trouble when loading images; then displays a warning on startScreen if so.

    private boolean RANDOM_PASTEL_BACKGROUND = true;    //  Randomizes the background into different-colored grids.

    //  --------------------------------------------        IMAGE STORAGE   -   BufferedImages and dimensions related to drawing cells

    private int backgroundCellMargin = 1;           //  Border within a background square, in pixels; used in generating the default background tile image.

    private BufferedImage backgroundTile;           //  Individual tile making up the generated background image
    private Graphics2D TileGraphics;

    private BufferedImage backgroundImage;          //  The background image that gets drawn onto the Canvas before anything else;
    private Graphics2D BGGraphics;
    private ArrayList<Color> pastelColors;          //  List of colors associated with the background, populated in populatePastelColors();

    private BufferedImage snakeTile;                //  Default ugly snake tile;
    private Graphics2D SGraphics;

    private BufferedImage treatTile;                //  Default ugly Treat tile
    private Graphics2D TGraphics;

    private BufferedImage endScreen;                //  Image that pops up when the game is over;
    private Graphics2D endGraphics;

    //  --------------------------------------------        MISC STORAGE
    private SnakeCell head;

    public java.util.Queue<String> audioQueue;      //  A queue containing names of audio files, which periodically get checked by the soundManager and played.
    private SoundManager soundManager;


    //  --------------------------------------------        CODE        ---------------------------------
    public static void main(String[] args){
        Launch launch = new Launch();
    }

    //  All function starts are housed in the Launch constructor.       There are inter-dependencies, so try not to alter the sequence too much.
    private Launch(){
        CustomToolkit.setParams();                  //  Sets the default image directory to System.getProperty("user.dir")/images/
        setUpGUI();

        initializeParams();

        SnakeCell.DEFAULT_BUFFERED_IMAGE = snakeTile;           //  Set the default image to an ugly default generated icon ;
        SnakeCell.setupStaticImageParams();                     //  Attempts to load the default images and dimensions to be used by instances of this class
                                                                //      if successful, DEFAULT_BUFFERED_IMAGE gets overridden.
        Treat.DEFAULT_BUFFERED_IMAGE = treatTile;
        Treat.setupStaticImageParams();                         //  Pretty much the same as above;

        if((SnakeCell.DEFAULT_BUFFERED_IMAGE==snakeTile) || (Treat.DEFAULT_BUFFERED_IMAGE==treatTile)){
            GRAPHICS_FAILED = true;
        }

        gui.setBackgroundImage(backgroundImage);            //  Set up the background image to be used in the Canvas

        refreshWorld();                                     //
        beginProgram();
    }

    //  Sets up a GUI director, in charge of the interface and graphics. Passes commands to the frame, the canvas, and other components.
    public void setUpGUI(){
        gui = new GUIDirector(this);
        gui.setLaunchReference(this);
        gui.setPaintDelay(PAINT_DELAY);
        cellWidth = GUIDirector.CANVAS_WIDTH / gridSize;
        cellHeight = GUIDirector.CANVAS_HEIGHT / gridSize;
    }

    //  Initializes the data structures for the program
    private void initializeParams(){
        gameObjects = new ArrayList<ArrayList>();

        audioQueue = new LinkedList<String>();
        soundManager = new SoundManager(this);
        soundManager.start();

        background = new ArrayList<DrawableObject>();
        gameObjects.add(background);

        inBetween = new ArrayList<DrawableObject>();
        gameObjects.add(inBetween);

        foreground = new ArrayList<DrawableObject>();
        gameObjects.add(foreground);

        initializeImages();     //  Creates BuffedImage and Graphics associated with images
        fillImages();           //  Fills them in with default tile patterns
    }

    //  Initializes the BufferedImages used, and sets up their graphics.
    private void initializeImages(){
        backgroundTile =  new BufferedImage(cellWidth,cellHeight, BufferedImage.TYPE_INT_ARGB);
        TileGraphics = (Graphics2D)backgroundTile.getGraphics();

        backgroundImage =  new BufferedImage(GUIDirector.CANVAS_WIDTH,GUIDirector.CANVAS_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        BGGraphics = (Graphics2D)backgroundImage.getGraphics();

        snakeTile = new  BufferedImage(cellWidth,cellHeight, BufferedImage.TYPE_INT_ARGB);
        SGraphics = (Graphics2D)snakeTile.getGraphics();

        treatTile = new  BufferedImage(cellWidth,cellHeight, BufferedImage.TYPE_INT_ARGB);
        TGraphics = (Graphics2D)treatTile.getGraphics();

        endScreen = (CustomToolkit.removeTileBackground(CustomToolkit.loadImage("Hud/endgame.png")));
        if(endScreen==null){
            GRAPHICS_FAILED = true;
            generateEndScreen();
        }
        endGraphics = (Graphics2D)endScreen.getGraphics();
        gui.endGameImage = endScreen;
    }

    //  Fills the images with hardcoded patterns
    private void fillImages(){
        generateBackgroundTile();
        generateSnakeTile();
        generateTreatTile();
        generateEndScreen();
        populatePastelColors();
        fillBackground();
    }

    //  Brain of the program; surrounded by try-catch because of the 'sleep' function.
    public void beginProgram() {
        if(GRAPHICS_FAILED){                    //  If true, display a warning on the start screen.
            gui.enableGraphicsWarning();
        }
        try{
            while (KEEP_PROCESSING) {
                Thread.sleep(LOGIC_DELAY);
                if(GUI_IS_READY) {
                    step();
                }
            }
        } catch (InterruptedException e) {
            System.out.println(Thread.currentThread().getName() + " was interrupted");
        }
    }

    //  Represents one logic step of the program
    //  REFRESH_WORLD is awkwardly checked twice, because it might change to 'true' during checkForCollisions();
    //      if not checked after that, snake will be re-drawn into a wall :p
    private void step(){
        checkForKeyboardActions();                  //  Checks for any keyboard actions performed and acts on them
        if(!REFRESH_WORLD) {                  //  If the world doesn't need to be reset (a potential resilt of a collision), then keep going
            calculateStep();                            //  Calculates the state of each object in the world;   params like 'nextLocation' are set.
            checkForCollisions();                       //  Checks for collisions between objects in the world
            //addCellToSnake();
            if(!REFRESH_WORLD) {
                makeTheWorldDance();                        //  call step() on every object in the world
                updateObjectImages();                       //  Update the image state of every object based on its current params
                gui.displayScore(GAME_SCORE);
            }
        }   else    {
            endGame();
        }
    }

    //  Checks for any keyboard commands stored within the GUIDirector.
    //  The keyboard commands are communicated to the processor through the <actionToPerform> String stored within the GUI, accessed via its getNextAction function
    private void checkForKeyboardActions(){
        String action = gui.getNextAction();
        //  If the action isn't blank
        if(!(action.equals(" "))){
            if(action.equals("SPACE")){
                if(REFRESH_WORLD==true){
                    System.out.println("Restarting world.");
                    refreshWorld();
                }
            }   else {
                char press = action.toLowerCase().charAt(0);
                //  And if commanded direction is not equal to current AND not the one opposite (no 180 turns allowed lol)
                if ((press != head.direction.getOpposite()) && (press != head.direction.getDirection())) {
                    //  ...then set current direction to key press.
                    head.setDirection(press);
                }
            }
        }
        gui.clearAction();
    }

    //  Recreates the game world. Requires the data structures to have been loaded.
    //  Resets the score and speed, clears all objects from the world, re-orients snake images, and re-draws the background.
    private void refreshWorld(){
        REFRESH_WORLD = false;

        GAME_SCORE = 0;
        gui.displayScore(GAME_SCORE);
        LOGIC_DELAY = DEFAULT_LOGIC_DELAY;          //  Reset game speed to default;

        clearDrawables();
        createObjects();    //  Createthe snake and a treat
        setSnakeImages();   //  Orient snake's body part images properly;

        gui.CONTINUE_REFRESHING_WORLD = true;

        fillBackground();   //  Re-fill the background
    }

    //  Call calculateStep() on every relevant object in the world; this does things like calculate the nextLocation of an object.
    public void calculateStep(){
        ArrayList<DrawableObject> tempObjList;
        int numOfLists = gameObjects.size();
        for(int listIndex = 0; listIndex < numOfLists; listIndex++){
            tempObjList = gameObjects.get(listIndex);
            int numOfObjects = gameObjects.get(listIndex).size();
            for(int objIndex = 0; objIndex < numOfObjects; objIndex++){
                DrawableObject tempObject = tempObjList.get(objIndex);
                if(tempObject!=null){
                    tempObject.calculateStep();
                }
            }
        }
    }

    //  Checks for collisions based every object's calculated new location and change the affected objects accordingly
    public void checkForCollisions(){
        if(cellIsWithinBounds(head.getNextLocation())){
            for(int i = 0; i < inBetween.size(); i++){
                DrawableObject treat = inBetween.get(i);
                if(head.locationIsWithinSnake(head.getNextLocation())){
                    System.out.println("bumped into itself. SILLY SNAKE");
                    audioQueue.add("bump.wav");
                    REFRESH_WORLD = true;
                }
                if(SnakeCell.isSameLocation(head.getNextGridLocation(),treat.getNextGridLocation())){
                    treatEaten(treat);
                }
            }
        }   else    {
            System.out.println("Starting over! Silly snake.");
            audioQueue.add("bump.wav");
            REFRESH_WORLD = true;
        }
    }

    //  Actually sets things in action by calling step() on every object in the world; changes locations and parameters of objects based on temporary variables assigned in calculateStep() and checkForCollisions()
    public void makeTheWorldDance(){
        ArrayList<DrawableObject> tempObjList;
        int numOfLists = gameObjects.size();
        for(int listIndex = 0; listIndex < numOfLists; listIndex++){
            tempObjList = gameObjects.get(listIndex);
            int numOfObjects = gameObjects.get(listIndex).size();
            for(int objIndex = 0; objIndex < gameObjects.get(listIndex).size(); objIndex++){
                DrawableObject tempObject = tempObjList.get(objIndex);
                if(tempObject!=null){
                    tempObject.step();
                }
            }
        }
    }

    //  Updates the iamge state of every object in the world.
    //  The reason this operation isn't bundled with the rest of the methods is because the way some objects look relies on locations of other objects, so their image can't be settled until the world around them is.
    //  Think of a straight piece of a snake needing to know that the cells behind and in front of it are truly in place.
    public void updateObjectImages(){
        ArrayList<DrawableObject> tempObjList;
        int numOfLists = gameObjects.size();
        for(int listIndex = 0; listIndex < numOfLists; listIndex++){
            tempObjList = gameObjects.get(listIndex);
            int numOfObjects = gameObjects.get(listIndex).size();
            for(int objIndex = 0; objIndex < gameObjects.get(listIndex).size(); objIndex++){
                DrawableObject tempObject = tempObjList.get(objIndex);
                if(tempObject!=null){
                    tempObject.updateObjectImage();
                }
            }
        }
    };

    //  Sets snake images based on body orientation. Otherwise everything looks like 'default.png' at the start.
    private void setSnakeImages(){
        ArrayList<SnakeCell> tempSnake = head.getSnakeCells();
        for(int i = 0; i < tempSnake.size(); i++){
            tempSnake.get(i).updateObjectImage();
        }
    }

    //  Create the in-game objects, like snake cells and treats.
    //  Gets called every time the world is refreshed.
    public void createObjects() {
        createSnake();
        createNewTreat();
    }

    //  Creates the snake and adds it to the images to be drawn.
    //  Only creates one SnakeCell. The rest of the snake is created recursively, with number of cells specified by the DEFAULT_LENGTH variable.
    private void createSnake(){
        head = new SnakeCell(10,10,DEFAULT_DIRECTION,DEFAULT_LENGTH,0);
        ArrayList<SnakeCell> cellList = head.getSnakeCells();
        for(int i = 0; i < cellList.size(); i++){
            addToForeground(cellList.get(i));
        }
    }

    //  Adds an extra cell to the snake and to the list of objects to be drawn.
    private void addCellToSnake(){
        SnakeCell newSnakeCell = head.addAnotherCell();
        addToForeground(newSnakeCell);
    }

    //  Creates a randomly-positioned treat somewhere on the map.
    private void createNewTreat(){
        Treat treat = new Treat();
        //  Create random grid coordinates for the location
        int[] coords = new int[2];
            do {    //  Loop generates random coordinates until they fall outside of the snake.
                coords[0] = CustomToolkit.randomUpToExcluding(gridSize);
                coords[1] = CustomToolkit.randomUpToExcluding(gridSize);
            }   while((head.locationIsWithinSnake(coords)));
        treat.setGridLocation(coords[0],coords[1]);
        //  ..and convert them to XY coordinates.
        int[] XYcoords = convertToCoords(coords[0],coords[1]);
        treat.setXYLocation(XYcoords[0],XYcoords[1]);
        addToInBetween(treat);
    }

    //  Function called when the treat is eaten.
    //  INPUT:  the Treat object that is eaten.
    private void treatEaten(DrawableObject treat){
        audioQueue.add("eat.wav");
        GAME_SCORE  +=  100;
        removeFromWorld(treat);                           //  Remove the treat from the world
        createNewTreat();                                 //  Create a new treat
        addCellToSnake();                                 //  Grow the snake
        LOGIC_DELAY = (int)((double)LOGIC_DELAY * 0.95);  //  Increase speed of game.
    }

    //  Fills the background with the background tile;
    //  if  RANDOM_PASTEL_BACKGROUND is enabled, fills the background with randomly-colored tiles pulled from pastelColors
    private void fillBackground() {
        for (int X = 0; X < gridSize; X++) {
            for (int Y = 0; Y < gridSize; Y++) {
                if(RANDOM_PASTEL_BACKGROUND){       //  If this is enabled, grid becomes a random palette of 'pastelColors'.
                    Color randomColor = pastelColors.get(CustomToolkit.randomUpToExcluding(pastelColors.size()));
                    TileGraphics.setColor(randomColor);
                    //TileGraphics.fillRect(backgroundCellMargin,backgroundCellMargin,cellWidth-2*backgroundCellMargin,cellHeight-2*backgroundCellMargin);
                    TileGraphics.fillRect(0,0,cellWidth,cellHeight);
                }
                BGGraphics.drawImage(backgroundTile, X*cellWidth, Y*cellHeight, null);
            }
        }
    }

    //  Populates the list pastelColors with these colors;
    //  The colors are then used to randomly fill in the background;
    private void populatePastelColors(){
        pastelColors = new ArrayList<Color>();
        pastelColors.add(new Color(193,255,193));       //  green1
        pastelColors.add(new Color(180,238,180));       //  green1
        pastelColors.add(new Color(155,205,155));       //  green1
        pastelColors.add(new Color(143,188,143));       //  green1
        pastelColors.add(new Color(193,205,193));       //  green1
        pastelColors.add(new Color(124,205,124));       //  green1
        pastelColors.add(new Color(162,205,90));        //  green1
    }

    //  Create a simple pattern for a background tile; currentlty a gray square with a black margin
    private void generateBackgroundTile(){
        TileGraphics.setColor(Color.BLACK);
        TileGraphics.fillRect(0,0,cellWidth,cellHeight);
        TileGraphics.setColor(Color.GRAY);
        TileGraphics.fillRect(backgroundCellMargin,backgroundCellMargin,cellWidth-2*backgroundCellMargin,cellHeight-2*backgroundCellMargin);
    }

    //  Manipulates Snake's BufferedImage to form a pattern.
    private void generateSnakeTile(){
        SGraphics.setColor(Color.BLACK);
        SGraphics.fillOval(0,0,cellWidth,cellHeight);
    }

    //  Draws a backup icon for Treat objects, in case it doesn't load from file;
    private void generateTreatTile(){
        //TGraphics.setColor(Color.RED);
        //TGraphics.fillOval(cellWidth/5,cellHeight/5,cellWidth/2,cellHeight/2);
//        BufferedImage treatIcon = CustomToolkit.loadImage("Treat/default.png");
//        treatIcon = CustomToolkit.removeTileBackground(treatIcon);

//        TGraphics.drawImage(treatIcon,0,0,null);

        TGraphics.setColor(Color.RED);
        TGraphics.fillRect(2,2,cellWidth/3,cellHeight/3);
    }

    private void generateEndScreen(){
        if(endScreen==null){
            endScreen =  new BufferedImage(250,100, BufferedImage.TYPE_INT_ARGB);
            endGraphics = ((Graphics2D)(endScreen.getGraphics()));
            endGraphics.setColor(Color.RED);
            endGraphics.fillRect(0,0,250,100);
            endGraphics.setColor(Color.WHITE);
            Font font = new Font("Verdana", Font.BOLD, 20);
            endGraphics.setFont(font);
            endGraphics.drawString("GAME ENDED LOL",10,30);
            endGraphics.drawString("PRESS SPACEBAR",10,60);
            endGraphics.drawString("TO UNDERP",10,80);
            gui.endGameImage = endScreen;
        }
    }

    //  Checks whether the given location is within the bounds of the grid.
    //  Lowest row/column value is 0 and highest is (gridSize-1)
    //  OUTPUT: false if outside of bounds; true if inside.
    private boolean cellIsWithinBounds(int[] loc){
        if(loc[0] < 0 || loc[0] >= gridSize || loc[1] < 0 || loc[1] >= gridSize){
            return false;
        }
        return true;
    }

    //  Converts Grid column and row to X and Y location on canvas, respectively.
    //  INPUT:  two ints, usually between 0 and (gridSize-1), inclusive.
    //  OUTPUT: corresponding X and Y coordinates on the canvas; (top left corner of image)
    private int[] convertToCoords(int gridX, int gridY){
        int[] coords = new int[2];
        coords[0] = gridX;
        coords[1] = gridY;
        return convertToCoords(coords);
    }

    //  Converts Grid column and row to X and Y location on canvas, respectively.
    //  INPUT:  an int[] of size 2, each value usually between 0 and (gridSize-1), inclusive.
    //  OUTPUT: corresponding X and Y coordinates on the canvas; (top left corner of image)
    private int[] convertToCoords(int[] gridXY){
        int[] temp = new int[2];
        temp[0] = gridXY[0]*cellWidth;
        temp[1] = gridXY[1]*cellHeight;
        return temp;
    }


    //  Adds a DrawableObject to the list of things to draw in the background;
    public void addToBackground(DrawableObject obj){
        if(!background.contains(obj)){
            background.add(obj);
        }
    }

    //  Adds a DrawableObject to the list of things to draw between the background and foreground;
    public void addToInBetween(DrawableObject obj){
        if(!inBetween.contains(obj)){
            inBetween.add(obj);
        }
    }

    //  Adds a DrawableObject to the list of things to draw in the foreground;
    public void addToForeground(DrawableObject obj){
        if(!foreground.contains(obj)){
            foreground.add(obj);
        }
    }

    //  Removes all Drawable objects from the screen;
    public void clearDrawables(){
        int max = gameObjects.size();
        for(int i = 0; i < max; i++){
            ArrayList<DrawableObject> list = gameObjects.get(i);
            list.clear();
        }
    }

    //  Removes a DrawableObject from the world
    public void removeFromWorld(DrawableObject obj){
        int max = gameObjects.size();
        for(int i = 0; i < max; i++){
            ArrayList<DrawableObject> list = gameObjects.get(i);
            if(list.contains(obj)){
                list.remove(obj);
            }
        }
    }

    //  Gets called when the game ends.
    private void endGame(){
        gui.CONTINUE_REFRESHING_WORLD = false;
    }
}
