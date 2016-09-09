import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by Swayze on 9/4/2016.
 *
 * The snake is a sort of a LinkedList, where the 'nextCell' points to the cell behind the current one.
 * The tail of the snake has a nextCell of 'null'
 *
 *
 */

public class SnakeCell extends DrawableObject {

    private String contents;
    private SnakeCell cellBehind;       //  The cell behind this one
    private SnakeCell cellAhead = null;        //  The cell ahead of this one
    public char oldDirection;
    public Direction direction;
    private String bodyPartType;

    private boolean isHead = false;
    private int length;

    //  DEFAULT STATIC PARAMETERS
    public static int DEFAULT_IMAGE_WIDTH;
    public static int DEFAULT_IMAGE_HEIGHT;
    public static String DEFAULT_IMAGE_PATH;

    public static BufferedImage DEFAULT_BUFFERED_IMAGE;

    public static HashMap<String,BufferedImage> imageMap;

    //  Sets up the default static parameters for all objects to share; called early on by the processor
    public static void setupStaticImageParams(){
        DEFAULT_BUFFERED_IMAGE = CustomToolkit.loadImage("SnakeCell\\default.png");
        DEFAULT_IMAGE_WIDTH = 20;
        DEFAULT_IMAGE_HEIGHT = 20;
        if(DEFAULT_BUFFERED_IMAGE != null){
            DEFAULT_BUFFERED_IMAGE = CustomToolkit.removeTileBackground(DEFAULT_BUFFERED_IMAGE);
            DEFAULT_IMAGE_WIDTH = DEFAULT_BUFFERED_IMAGE.getWidth();
            DEFAULT_IMAGE_HEIGHT = DEFAULT_BUFFERED_IMAGE.getHeight();
        }   else    {
            System.out.println("SnakeCell was not able to load image from setupStaticImageParams");
        }
        setUpImageMap();
    }

    //  Creates and populates the imageMap where the object's BufferedImage icons are stored.
    public static void setUpImageMap(){
        imageMap = new HashMap<String,BufferedImage>();
        String[] straightParts  =   {"head_","tail_","body_"};
        String[] straightRotations = {"u","r","d","l"};
        String[] cornerRotations = {"ur","dr","dl","ul"};
        for(int i = 0; i < straightParts.length; i++){
            generateImagesViaRotation(straightParts[i],straightRotations);
        }
        generateImagesViaRotation("body_",cornerRotations);
}
    //  Populates imageMap with four images
    //  Each image is created by rotating the original by a multiple of 90 degrees.
    private static void generateImagesViaRotation(String prefix, String[] suffices){
        BufferedImage img = (CustomToolkit.removeTileBackground(CustomToolkit.loadImage("SnakeCell\\" + prefix + suffices[0] + ".png")));
        for(int i = 0; i < 4; i++){
            String s = prefix + suffices[i];
            imageMap.put(s,CustomToolkit.rotateImage(img,Math.toRadians(i*90)));
        }
    }

    public SnakeCell(){
        this(0,0,'u',3,0);
    }

    //  Main constructor to create a SnakeCell; the rest of the snake is created recursively based on the 'length' parameter passed
    //  INPUTS:     <int>   gridX and gridY --  Where the cell is to be placed on the grid, in column (X) and row (Y)
    //              <char>  dir             --  Direction the cell should face  (u,d,l,r)
    //              <int>   length          --  Desired length of the snake; gets decremented as the constructor calls itself
    //              <int>   counter         --  An internal parameter to help identify the 'head'; gets incremented as constructor calls itself
    public SnakeCell(int gridX, int gridY, char dir, int length, int counter){

        if(counter==0){         //  If the counter is zero, that means this is the head.
            isHead=true;
        } if(length!=0){        //  If length is not zero, means there's more cells to be made
            setSize(DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT);
            setIcon(DEFAULT_BUFFERED_IMAGE);
            setGridLocation(gridX, gridY);
            setDirection(dir);
            setLength(length);
            if(length > 1) {    //  If length is 1, means current one should be the last cell; create no more.
                //  calculate the location behind the current cell based on current direction and location
                int[] locationBehind = CustomToolkit.fuseArrays(direction.getReverseIncrement(),getGridLocation());
                //  create the next cell at the location behind this one and facing the same direction, passing length-- and counter++
                setCellBehind(new SnakeCell(locationBehind[0],locationBehind[1],getDirection(),length-1,counter+1));
                //  set its cellAhead parameter to the current cell;
                getCellBehind().setCellAhead(this);
            }
        }
    }

    public void debugTest(){
        System.out.println("Current grid location of cell: [" + gridLocation[0] + "][" + gridLocation[1] + "]");
        System.out.println("Direction facing: " + getDirection());
        int[] nextMovementIncrement = direction.getIncrement();
        System.out.println("At next move, the coordinates will change by [" + nextMovementIncrement[0] + "][" + nextMovementIncrement[1] + "]");
        int[] nextLoc = CustomToolkit.fuseArrays(getGridLocation(),nextMovementIncrement);
        System.out.println("After next step, the cell will be at [" + nextLoc[0] + "][" + nextLoc[1] + "]");
        char reverseDir = direction.getOpposite();
        System.out.println("Reverse direction: " + reverseDir);
        int[] reverseMovementIncrement = direction.getReverseIncrement();
        System.out.println("If stepping backward, coordinates would change by [" + reverseMovementIncrement[0] + "][" + reverseMovementIncrement[1] + "]");
        int[] locationBehind = CustomToolkit.fuseArrays(reverseMovementIncrement,getGridLocation());
        System.out.println("After backward step, the cell would be at [" + nextLoc[0] + "][" + nextLoc[1] + "]");
    }

    //  This gets called by the processor;  normally I'd let objects do their own thing, but the order in which objects are called on is complicated and messes the snake up
    //      So this gets called on the its Head only, which takes care of the rest in careful sequence.
    public void calculateStep(){
        if(isHead()){
            calculateNextLocationsOfcells();                //  Calculate and set future locations of the cells behind this one
            updateLimbDirectionsOfCells(getDirection());    //  Calculate and set current directions of cells
        }
    }

    //  Gets called by the processor. May need to delegate this to the Head as well...
    public void step(){
        moveCellForward();
    }

    //  Updates the object's icon based on current status
    public void updateObjectImage(){
        setBodyPartType(determineBodyPartType());   //  Adjust object's icon based on orientation within the snake.
        setIcon(imageMap.get(getBodyPartType()));   //  Set the icon
    }

    public void calculateNextLocationsOfcells(){
        nextGridLocation = null;                    //  Reset nextGridLocation calculated in previous cycle
        if(hasCellBehind()){
            getCellBehind().calculateNextLocationsOfcells();
        }
        nextGridLocation = CustomToolkit.fuseArrays(direction.getIncrement(),getGridLocation());
    }

    public void moveCellForward(){
        setGridLocation(nextGridLocation[0],nextGridLocation[1]);
    }

    private void updateLimbDirectionsOfCells(char newDir){
        if(hasCellBehind()){
            cellBehind.updateLimbDirectionsOfCells(direction.getDirection());
        }
        oldDirection = getDirection();
        setDirection(newDir);
    }

    //  Adds a cell to the tail of the snake
    public SnakeCell addAnotherCell(){
        if(hasCellBehind()){
            return cellBehind.addAnotherCell();
        }   else    {
            //  Calculate the location of the new cell based on current location of the last cell and its OLD direction - at this point in the process, its direction has changed. Spent hours debugging this to get it to work...
            int[] locationBehindTail = CustomToolkit.fuseArrays(Direction.getReverseIncrement(oldDirection),getGridLocation());
            SnakeCell newCell = new SnakeCell(locationBehindTail[0],locationBehindTail[1],direction.getDirection(),1,1);
            setCellBehind(newCell);                         //  Sets the link behind the current cell to the new cell
            newCell.setCellAhead(this);                     //  Sets the new cell's forward link to this cell
            newCell.direction.setDirection(oldDirection);   //  Sets the new cell's direction to current cell's old direction
            newCell.nextGridLocation = CustomToolkit.fuseArrays(newCell.direction.getIncrement(),newCell.getGridLocation());    //  Sets newCells nextDirection
            return newCell;
        }
    }

    //  Returns the SnakeCell at the tail
    public SnakeCell getLastCell(){
        if(hasCellBehind()){
            return cellBehind.getLastCell();
        }
        return this;
    }

    //  Calculate and return the length of the snake, recursively
    public int getLengthOfBody(int counter){
        counter+=1;
        if(hasCellBehind()){
            return getCellBehind().getLengthOfBody(counter);
        }
        return counter;
    }

    //  Return an ArrayList of all SnakeCells linked to this one
    public ArrayList<SnakeCell> getSnakeCells(){
        ArrayList<SnakeCell> snake = new ArrayList<SnakeCell>();
        addCellToList(snake);
        return snake;
    }

    //  Adds the current cell to the passed list
    //  If nextCell exists, calls this method on it
    public void addCellToList(ArrayList<SnakeCell> list){
        if(cellBehind!=null){
            cellBehind.addCellToList(list);
        }
        list.add(this);
    }

    //  Sets length to passed parameter.
    public void setLength(int l){
        length = l;
    }

    public boolean locationIsWithinSnake(int[] loc){
        if(isSameLocation(getGridLocation(),loc)){
            return true;
        }   else if(hasCellBehind()){
            return getCellBehind().locationIsWithinSnake(loc);
        }
        return false;
    }

    //  Deprecated... may not be used at all
    public void moveSnakeCellTo(int x, int y){
        if(cellBehind!=null) {
            cellBehind.moveSnakeCellTo(gridLocation[0],gridLocation[1]);
        }
        gridLocation[0] = x;
        gridLocation[1] = y;
        setXYLocation(x*IMAGE_WIDTH, y*IMAGE_HEIGHT);
    }

    //  Returns the location of the Cell on the grid.
    public int[] getGridLocation(){
        return gridLocation;
    }

    public static boolean isSameLocation(int[] loc1, int[] loc2){
        return ((loc1[0] == loc2[0]) && (loc1[1] == loc2[1]));
    }

    //  Returns true if the cell has a cell linked behind it
    public boolean hasCellBehind(){
        return (cellBehind != null);
    }

    //  Returns true if the cell has a cell linked ahead of it
    public boolean hasCellAhead(){
        return (cellAhead != null);
    }

    //  Returns grid coordinates of the next location of the cell, were it to step forward.
    public int[] getNextLocation(){
        return nextGridLocation;
        //return CustomToolkit.fuseArrays(direction.getIncrement(),getGridLocation());
    }

    public SnakeCell getCellBehind(){
        return cellBehind;
    }

    public void setCellBehind(SnakeCell cell){
        cellBehind = cell;
    }

    public SnakeCell getCellAhead(){
        return cellAhead;
    }

    public void setCellAhead(SnakeCell cell){
        cellAhead = cell;
    }

    public int getLength(){
        return length;
    }

    private void toggleHead(){
        isHead = !isHead;
    }

    private boolean isHead(){
        return isHead;
    }



    //  Set current direction of the cell; the direction determines where the cell moves.
    //  If the cell is a head, set the direction of the cell behind it to that of the head, so decapitation does not take place.
    public void setDirection(char c){
        if(direction==null){
            direction = new Direction(c);
        }
        direction.setDirection(c);
    }

    //  Returns a <char> representing the current direction
    public char getDirection(){
        return direction.getDirection();
    }

    public void setBodyPartType(String s){
        bodyPartType = s;
    }

    public String getBodyPartType(){
        return bodyPartType;
    }

    //  Complex code that tries to determine the body part the cell belongs to, and its orientation relative to the adjacent two cells;
    //  Returns a string like "head_r" or "body_ur" or "tail_d" which should correspond to the name of a BufferedImage
    //  Letters after the underscore signify orintation of the cell;
    //  Picture the snake curled up in a circle:                        ul - ur
    //                                                                   |   |
    //                                                                  dl - dr
    public String determineBodyPartType(){
        String newPart = " ";
        if(!hasCellAhead()){
            //  If the cell has nothing ahead of it, its a Head (Ha. Pun)
            newPart = "head_" + getDirection();     //  add a direction to it, and return
        }   else if(!hasCellBehind()){
            //  if there's nothing behind it, it's a tail
            newPart = "tail_" + getDirection();      //  add a direction to it, and return
        }   else    {
            //  If it has both of those, it's a "body_" part
            newPart = "body_";
            //  Get directions toward the cell ahead and behind, based on their grid coordinates.
            char dirToAhead = getDirectionTo(cellAhead);
            char dirToBehind = getDirectionTo(cellBehind);

            //  If the cell ahead is directly in front and the one behind is directly behind (confusing, yeah) then it's just a straight line.
            if(getDirection()==dirToAhead && direction.getOpposite()==dirToBehind){ newPart+=getDirection();   }
            //  CORNERS
            //  If their directions are to the right and bottom, then it's a top-left corner.
            else if((dirToAhead=='r' && dirToBehind=='d') || (dirToAhead=='d' && dirToBehind=='r')){ newPart+="ul"; }
            //  If their directions are to the left and bottom, then it's a top-right corner.
            else if((dirToAhead=='l' && dirToBehind=='d') || (dirToAhead=='d' && dirToBehind=='l')){ newPart+="ur"; }
            //  If their directions are to the right and top, then it's a bottom-left corner.
            else if((dirToAhead=='r' && dirToBehind=='u') || (dirToAhead=='u' && dirToBehind=='r')){ newPart+="dl"; }
            //  If their directions are to the left and top, then it's a bottom-right corner.
            else if((dirToAhead=='l' && dirToBehind=='u') || (dirToAhead=='u' && dirToBehind=='l')){ newPart+="dr"; }
        }
        return newPart;
    }

    private char getDirectionTo(SnakeCell cell){
        return Direction.convertIncrementToDir(getGridDifferenceTo(cell));
    }

    //  Returns an int[] that specifies change in coordinates required to get to passed 'cell'
    //  difference[0] is change in column
    //  difference[1] is change in row
    private int[] getGridDifferenceTo(SnakeCell cell){
        int[] original = getGridLocation();
        int[] forward = cell.getGridLocation();
        int[] difference = new int[2];
        difference[0] = forward[0] - original[0];
        difference[1] = forward[1] - original[1];
        return difference;
    }


}
