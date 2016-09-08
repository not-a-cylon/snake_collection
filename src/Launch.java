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
    private int DEFAULT_LENGTH = 3; //  Starting length of the snake.

    //  --------------------------------------------        INTERNAL VARS    -   vars used by the system.
    private ArrayList<Treat> listOfTreats;      //  A makeshift list storing created treats.
    private DrawableObject[][] grid;            //  A 2D grid for idontknowwhat deprecated.

    //  --------------------------------------------        INTERFACE DIMENSIONS
    private int FRAME_WIDTH = 500;          //  width and height of the main frame.
    private int FRAME_HEIGHT = 500;

    private int DISPLAY_WIDTH = 400;        //  width and height of the display panel.
    private int DISPLAY_HEIGHT = 400;

    private int cellWidth;                  //  width and height of the cell, CALCULATED based on gridSize;
    private int cellHeight;

    private int PAINT_DELAY = 10;           //  delay before painting each frame;

    private int LOGIC_DELAY = 200;           //  delay between steps in game logic (if it's even needed)

    private boolean KEEP_GOING = true;       //  Boolean that runs the game; if set to false, processes stop.

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

    //  All function starts are housed in the Launch constructor.
    private Launch(){
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
        initializeImages();
        fillImages();
        listOfTreats = new ArrayList<Treat>();
    }

    //  Initializes the BufferedImages used, and sets up their graphics.
    private void initializeImages(){
        backgroundTile =  new BufferedImage(cellWidth,cellHeight, BufferedImage.TYPE_INT_ARGB);
        TileGraphics = (Graphics2D)backgroundTile.getGraphics();

        backgroundImage =  new BufferedImage(DISPLAY_WIDTH,DISPLAY_HEIGHT, BufferedImage.TYPE_INT_ARGB);
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
                ArrayList<SnakeCell> snake = head.getSnakeCells();
                for(int i = 0; i < snake.size(); i++){
                    SnakeCell temp = snake.get(i);
                    temp.step();
                    //System.out.println(temp.getBodyPartType());
                    //System.out.println(temp.getBodyPartType() + " is a head: " + temp.getLength());
                }
                //refreshDrawables();
                //square.setX(square.getX()+1);       //  location of the dummy is incremented just for show. Add real program logic here.
            }
        } catch (InterruptedException e) {
            System.out.println(Thread.currentThread().getName() + " was interrupted");
        }
    }

    //  Represents one logic step of the program
    private void step(){
        checkForKeyboardActions();
        int[] nextLocation = head.getNextLocation();
        if(cellIsWithinBounds(nextLocation)){
            //head.direction.setOpposite();
            head.moveSnakeForward();
            Treat treat = returnTreatAtLocation(nextLocation);
            if(treat!=null){
                treatEaten(treat);
            }
        }   else    {
            System.out.println("Starting over! Silly snake.");
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

    //  Sets up a GUI director, in charge of the interface and graphics. Passes commands to the frame, the canvas, and other components.
    public void setUpGUI(){
        gui = new GUIDirector(FRAME_WIDTH,FRAME_HEIGHT,DISPLAY_WIDTH,DISPLAY_HEIGHT);
        gui.setPaintDelay(PAINT_DELAY);
        grid = new DrawableObject[gridSize][gridSize];
        cellWidth = DISPLAY_WIDTH / gridSize;
        cellHeight = DISPLAY_HEIGHT / gridSize;
    }

    //  Recreates the game world. Requires the data structures to have been loaded.
    private void refreshWorld(){
        LOGIC_DELAY = 200;
        gui.clearDrawables();
        listOfTreats.clear();
        createObjects();
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
            gui.addToForeground(cellList.get(i));
        }
    }

    //  Adds an extra cell to the snake and adds it to the gui to draw.
    private void addCellToSnake(){
        SnakeCell newSnakeCell = head.addAnotherCell();
        gui.addToForeground(newSnakeCell);
    }

    //  Creates a randomly-positioned treat somewhere on the map.
    private void createNewTreat(){
        Treat treat = new Treat(cellWidth,cellHeight);
        treat.setIcon(treatTile);
        //  Create random grid coordinates for the location
        int[] coords = new int[2];
        coords[0] = CustomToolkit.randomUpToExcluding(gridSize);
        coords[1] = CustomToolkit.randomUpToExcluding(gridSize);
        treat.setGridLocation(coords[0],coords[1]);
        //  ..and convert them to XY coordinates.
        int[] XYcoords = convertToCoords(coords[0],coords[1]);
        treat.setXYLocation(XYcoords[0],XYcoords[1]);
        listOfTreats.add(treat);
        gui.addToBackground(treat);
    }


    //  Checks if any existing treats are at the specified location on the grid.
    //  INPUT:  grid coordinates as int[2]
    //  OUTPUT: if found, the treat at the location. If not, returns null.
    private Treat returnTreatAtLocation(int[] gridCoords){
        int max = listOfTreats.size();
        for(int i = 0; i < max; i++){                   //  Cycle through the list of existing treats
            Treat treat = listOfTreats.get(i);
            if(treat.sameGridLocationAs(gridCoords)){   //  If the treat's location matches taht of the one passed,
                return treat;                           //  Return it;
            }
        }
        return null;                                    //  Otherwise return null.
    }

    //  Function called when the treat is eaten.
    //  INPUT:  Treat that is eaten.
    private void treatEaten(Treat treat){
        removeTreatFromWorld(treat);                                        //  Remove the treat from the world
        createNewTreat();                                                      //  Create a new treat
        addCellToSnake();                                                   //  Grow the snake
        System.out.println("The snake ate a treat!");
        System.out.println("Current length: " + head.getLengthOfBody(0));
        LOGIC_DELAY = (int)((double)LOGIC_DELAY * 0.9);                     //  Increase speed of game.
    }

    //  Removes the Treat from list of existing treat, and from list of objects to draw;
    //  INPUT:  Treat to remove.
    public void removeTreatFromWorld(Treat treat){
        listOfTreats.remove(treat);
        gui.removeFromScreen(treat);
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
}
