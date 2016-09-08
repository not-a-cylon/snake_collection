/**
 * Created by Swayze on 9/6/2016.
 *
 * The class exists in order to compartmentalize a bunch of functions. Things get very messy and confusing if they're instead attached to some other object.
 *
 */
public class Direction {

    private static char uVal = 'u';
    private static char dVal = 'd';
    private static char lVal = 'l';
    private static char rVal = 'r';

    private char direction;

    public Direction(char dir){
        direction = dir;
    }

    public void setDirection(char d){
        direction = d;
    }

    public char getDirection(){
        return direction;
    }

    //  Sets current direction to one opposite of it.
    public void setOpposite(){
        setDirection(getOpposite());
    }

    //  Gets the direction opposite of the one currently faced.
    public char getOpposite(){
        char temp = ' ';
        if(direction==dVal){    temp = uVal;    }
        if(direction==uVal){    temp = dVal;    }
        if(direction==lVal){    temp = rVal;    }
        if(direction==rVal){    temp = lVal;    }
        return temp;
    }

    //  Turns current direction 90 degrees clockwise;
    public void set90ClockWise(){
        setDirection(get90ClockWise());
    }

    //  Returns a direction 90 degrees clockwise of current one.
    public char get90ClockWise(){
        char temp = ' ';
        if(direction==uVal){    temp = rVal;    }
        if(direction==rVal){    temp = dVal;    }
        if(direction==dVal){    temp = lVal;    }
        if(direction==lVal){    temp = uVal;    }
        return temp;
    }

    //  Turns current direction 90 degrees counterclockwise;
    public void set90CounterClockWise(){
        setDirection(get90CounterClockWise());
    }

    //  Returns a direction 90 degrees counterclockwise of current one.
    public char get90CounterClockWise(){
        char temp = ' ';
        if(direction==uVal){    temp = lVal;    }
        if(direction==lVal){    temp = dVal;    }
        if(direction==dVal){    temp = rVal;    }
        if(direction==rVal){    temp = uVal;    }
        return temp;
    }

    //  Sets the current direction to a random one.
    public void setRandomDirection(){
        direction = getRandomDirection();
    }

    //  Sets the current direction to a random one, EXCEPT to the one specified.
    public void setRandomDirectionExcept(char exception){
        direction = getRandomDirectionExcept(exception);
    }

    //  Sets the current direction to a random one, EXCEPT to the one specified.
    public static char getRandomDirectionExcept(char exception){
        char temp = ' ';
        do{
            temp = getRandomDirection();
        }   while(temp!=exception);
        return temp;
    }

    //  Get a plain ol' random direction.
    public static char getRandomDirection(){
        char temp = ' ';
        int rand = CustomToolkit.randomUpToExcluding(4);
        if(rand == 0){  temp = uVal;    }
        if(rand == 1){  temp = dVal;    }
        if(rand == 2){  temp = lVal;    }
        if(rand == 3){  temp = rVal;    }
        return temp;
    }

    //  Turns current direction randomly counter- or clockwise.
    public void turn90randomly(){
        direction = get90randomly();
    }

    //  Returns a random direction that's 90 degrees either counter- or clockwise of the current one.
    public char get90randomly(){
        char temp = ' ';
        int rand = CustomToolkit.randomUpToExcluding(2);
        if(rand==0){    temp = get90ClockWise();    }
        if(rand==1){    temp = get90CounterClockWise();    }
        return temp;
    }

    //  Returns the XY increment if the direction were suddenly reversed.
    //  Could have done it using getOpposite().getIncrement(), but getOpposite returns a char and not a dir. Too much hassle to recode everything.
    public int[] getReverseIncrement(){
        int[] forward = getIncrement();
        int[] reverse = new int[2];
        for(int i = 0; i < forward.length; i++){
            reverse[i] = forward[i]*-1;
        }
        return reverse;
    }

    //  Returns the current direction as a change in XY coordinates.
    public int[] getIncrement(){
        int[] increment = new int[2];
        if(getDirection()==lVal){ increment[0] = -1; }
        if(getDirection()==rVal){ increment[0] = 1; }
        if(getDirection()==uVal){ increment[1] = -1; }
        if(getDirection()==dVal){ increment[1] = 1; }
        return increment;
    }

    public static char convertIncrementToDir(int[] step){
        char c = ' ';
        if(step[0]==1 && step[1]==0){       c = 'r'; }
        if(step[0]==-1 && step[1]==0){      c = 'l'; }
        if(step[0]==0 && step[1]==1){       c = 'd'; }
        if(step[0]==0 && step[1]==-1){       c = 'u'; }
        return c;
    }

    //  These two check if the direction passed is the same as the one faced currently.
    public boolean isSameAs(Direction newDir){
        return isSameAs(newDir.getDirection());
    }

    public boolean isSameAs(char newDir){
        return getDirection()==newDir;
    }

}
