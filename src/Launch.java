import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Swayze on 9/4/2016.
 * The brain of the program, subject to change drastically based on whatever program the panels and frames end up being used for.
 */
public class Launch {

    private GUIDirector gui;

    //  --------------------------------------------        CONSOLE VARS    -   change these to change outlook of the game. Intended to be intuitive.
    private int gridSize = 20;      //  The number of cells, in one direction, that the world should be split into.
    private int DEFAULT_LENGTH = 8; //  Starting length of the snake.

    //  --------------------------------------------        INTERNAL VARS    -   vars used by the system.
    private ArrayList<Treat> listOfTreats;      //  A makeshift list storing created treats.
    public static ArrayList<ArrayList> gameObjects;
    public static ArrayList<DrawableObject> background;
    public static ArrayList<DrawableObject> inBetween;
    public static ArrayList<DrawableObject> foreground;

    //  --------------------------------------------        INTERFACE DIMENSIONS
    private int cellWidth;                  //  width and height of the cell, CALCULATED based on gridSize;
    private int cellHeight;

    private int PAINT_DELAY = 10;           //  delay before painting each frame;

    private int DEFAULT_LOGIC_DELAY = 100;
    private int LOGIC_DELAY;           //  delay between steps in game logic (if it's even needed)

    private boolean KEEP_GOING = true;       //  Boolean that runs the game; if set to false, processes stop.
    private boolean REFRESH_WORLD = false;

    //  --------------------------------------------        IMAGE STORAGE   -   BufferedImages and dimensions related to drawing cells

    private int backgroundCellMargin = 1;           //  Border within a background square, in pixels.

    private BufferedImage backgroundTile;
    private Graphics2D TileGraphics;

    private BufferedImage backgroundImage;
    private Graphics2D BGGraphics;

    private BufferedImage snakeTile;
    private Graphics2D SGraphics;

    private BufferedImage treatTile;
    private Graphics2D TGraphics;

    //  --------------------------------------------        MISC STORAGE
    private SnakeCell head;

    //  --------------------------------------------        CODE        ---------------------------------
    public static void main(String[] args){
        Launch launch = new Launch();
    }

    //  All function starts are housed in the Launch constructor.       There are inter-dependencies, so try not to alter the sequence too much.
    private Launch(){
        CustomToolkit.setParams();
        GUIDirector.setupStaticParams();
        setUpGUI();
        SnakeCell.setupStaticImageParams();
        Treat.setupStaticImageParams();
        initializeParams();
        gui.setBackgroundImage(backgroundImage);
        refreshWorld();
        beginProgram();
    }

    //  Initializes the data structures for the program
    private void initializeParams(){
        listOfTreats = new ArrayList<Treat>();
        gameObjects = new ArrayList<ArrayList>();

        background = new ArrayList<DrawableObject>();
        gameObjects.add(background);

        inBetween = new ArrayList<DrawableObject>();
        gameObjects.add(inBetween);

        foreground = new ArrayList<DrawableObject>();
        gameObjects.add(foreground);

        initializeImages();
        fillImages();

    }

    //  Initializes the BufferedImages used, and sets up their graphics.
    private void initializeImages(){
        backgroundTile =  new BufferedImage(cellWidth,cellHeight, BufferedImage.TYPE_INT_ARGB);
        TileGraphics = (Graphics2D)backgroundTile.getGraphics();

        backgroundImage =  new BufferedImage(GUIDirector.PANEL_WIDTH,GUIDirector.PANEL_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        BGGraphics = (Graphics2D)backgroundImage.getGraphics();

        snakeTile = new  BufferedImage(cellWidth,cellHeight, BufferedImage.TYPE_INT_ARGB);
        SGraphics = (Graphics2D)snakeTile.getGraphics();

        treatTile = new  BufferedImage(cellWidth,cellHeight, BufferedImage.TYPE_INT_ARGB);
        TGraphics = (Graphics2D)treatTile.getGraphics();
    }

    //  Fills the images with hardcoded patterns
    private void fillImages(){
        generateBackgroundTile();
        generateSnakeTile();
        generateTreatTile();
        fillBackground();
    }

    //  Brain of the program; surrounded by try-catch because of the 'sleep' method.
    public void beginProgram() {
        try{
            while (KEEP_GOING) {
                Thread.sleep(LOGIC_DELAY);
                step();
                //System.out.println("---------------------");

            }
        } catch (InterruptedException e) {
            System.out.println(Thread.currentThread().getName() + " was interrupted");
        }
    }

    //  Represents one logic step of the program
    private void step(){
        checkForKeyboardActions();                  //  Checks for any keyboard actions performed and acts on them
        calculateStep();                            //  Calculates the state of each object in the world;   params like 'nextLocation' are set.
        checkForCollisions();                       //  Checks for collisions between objects in the world
        if(REFRESH_WORLD==false) {                  //  If the world doesn't need to be reset (a potential resilt of a collision), then keep going
            makeTheWorldDance();                        //  call step() on every object in the world
            updateObjectImages();                       //  Update the image state of every object based on its current params
        }   else    {
            REFRESH_WORLD = false;                      //  if the world needs to be reset, then do it.
            refreshWorld();
        }
    }

    //  Checks for any keyboard commands stored within the GUIDirector.
    //  The keyboard commands are communicated to the processor through the <actionToPerform> String stored within the GUI, accessed via its getNextAction function
    private void checkForKeyboardActions(){
        String action = gui.getNextAction();
        //  If the action isn't blank
        if(!(action.equals(" "))){
            char press = action.toLowerCase().charAt(0);
            //  And if commanded direction is not equal to current AND not the one opposite (no 180 turns allowed lol)
            if((press != head.direction.getOpposite()) && (press != head.direction.getDirection())){
                //  ...then set current direction to key press.
                head.setDirection(press);
            }
        }
        gui.clearAction();
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
                    REFRESH_WORLD = true;
                }
                if(SnakeCell.isSameLocation(head.getNextGridLocation(),treat.getNextGridLocation())){
                    treatEaten(treat);
                }
            }
            /*Treat treat = returnTreatAtLocation(nextHeadLocation);
            if(treat!=null){
                treatEaten(treat);
            }*/
        }   else    {
            System.out.println("Starting over! Silly snake.");
            REFRESH_WORLD = true;
            //refreshWorld();
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

    //  Sets up a GUI director, in charge of the interface and graphics. Passes commands to the frame, the canvas, and other components.
    public void setUpGUI(){
        gui = new GUIDirector();
        gui.setPaintDelay(PAINT_DELAY);
        cellWidth = GUIDirector.PANEL_WIDTH / gridSize;
        cellHeight = GUIDirector.PANEL_HEIGHT / gridSize;
    }

    //  Recreates the game world. Requires the data structures to have been loaded.
    private void refreshWorld(){
        LOGIC_DELAY = DEFAULT_LOGIC_DELAY;
        clearDrawables();
        listOfTreats.clear();
        createObjects();
        setSnakeImages();
    }

    //  Sets snake images based on body orientation. Otherwise everything looks like 'default.png' at the start.
    private void setSnakeImages(){
        ArrayList<SnakeCell> tempSnake = head.getSnakeCells();
        for(int i = 0; i < tempSnake.size(); i++){
            tempSnake.get(i).updateObjectImage();
        }
    }

    //  Create the in-game objects, like snake cells and treats.
    public void createObjects() {
        createSnake();
        createNewTreat();
    }

    //  Creates the snake and adds it to the images to be drawn.
    //  Only creates one SnakeCell. The rest of the snake is created recursively, with number of cells specified by the DEFAULT_LENGTH variable.
    private void createSnake(){
        head = new SnakeCell(10,10,'r',DEFAULT_LENGTH,0);
        ArrayList<SnakeCell> cellList = head.getSnakeCells();
        for(int i = 0; i < cellList.size(); i++){
            addToForeground(cellList.get(i));
        }
    }

    //  Adds an extra cell to the snake and adds it to the gui to draw.
    private void addCellToSnake(){
        SnakeCell newSnakeCell = head.addAnotherCell();
        addToForeground(newSnakeCell);
    }

    //  Creates a randomly-positioned treat somewhere on the map.
    private void createNewTreat(){
        Treat treat = new Treat(cellWidth,cellHeight);
        treat.setIcon(treatTile);
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
    //  INPUT:  Treat that is eaten.
    private void treatEaten(DrawableObject treat){
        removeFromWorld(treat);                                                 //  Remove the treat from the world
        createNewTreat();                                                       //  Create a new treat
        addCellToSnake();                                                       //  Grow the snake
        System.out.println("Current length: " + head.getLengthOfBody(0));
        LOGIC_DELAY = (int)((double)LOGIC_DELAY * 0.95);                       //  Increase speed of game.
    }

    //  Fills the background with the background tile;
    private void fillBackground() {
        for (int X = 0; X < gridSize; X++) {
            for (int Y = 0; Y < gridSize; Y++) {
                BGGraphics.drawImage(backgroundTile, X*cellWidth, Y*cellHeight, null);
            }
        }
    }

    //  Create a simple pattern for a background square.
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

    //  Manipulates a background tile's BufferedImage to form a pattern.
    private void generateTreatTile(){
        TGraphics.setColor(Color.RED);
        TGraphics.fillOval(cellWidth/5,cellHeight/5,cellWidth/2,cellHeight/2);
        //BufferedImage treatIcon = CustomToolkit.loadImage("Treat.png");
        //treatIcon = CustomToolkit.removeTileBackground(treatIcon);
        //TGraphics.drawImage(treatIcon,0,0,null);
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


    //  Adds a DrawableObject to the drawables list, which is then drawn onscreen by the panel at every cycle.
    public void addToBackground(DrawableObject obj){
        if(!background.contains(obj)){
            background.add(obj);
        }
    }

    public void addToInBetween(DrawableObject obj){
        if(!inBetween.contains(obj)){
            inBetween.add(obj);
        }
    }

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

    //  Removes a DrawableObject from the drawables list, which is then drawn onscreen by the panel at every cycle.
    public void removeFromWorld(DrawableObject obj){
        int max = gameObjects.size();
        for(int i = 0; i < max; i++){
            ArrayList<DrawableObject> list = gameObjects.get(i);
            if(list.contains(obj)){
                list.remove(obj);
            }
        }
    }
}
