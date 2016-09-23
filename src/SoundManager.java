import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Swayze on 9/15/2016.
 *
 * This runs in a separate thread. It's job is to periodically check for sound requests from the Launch class and play them as directed.
 * If sounds are run through the main game logic thread, they may temporarily halt gameplay. Better to have a separate thread for them.
 *
 * Just need to initialize it and call start() on its instance to have it keep checking.
 *
 */
public class SoundManager extends Thread{

    AudioStream as;                         //  Might not even be needed.

    private int CHECK_DELAY = 100;          //  Pause between scanning for files to play;
    public boolean CONTINUE_CHECKING;       //  While set to 'true', will keep playing.

    Launch launchReference;                 //  A reference to the Launch class

    public SoundManager(Launch launch){
        CONTINUE_CHECKING = true;
        launchReference = launch;
    }

    public void run(){
        try{
            while(CONTINUE_CHECKING) {          //  This is always set to true;
                Thread.sleep(CHECK_DELAY);
                checkForSoundRequests();
            }
        } catch(InterruptedException e) {
            System.out.println(Thread.currentThread().getName() + " was interrupted");
        }
    }

    //  Checks for names of sound files to play, stored in a queue by the launchReference object
    private void checkForSoundRequests(){
        if(launchReference.audioQueue!=null){
            String file = launchReference.audioQueue.poll();    //  Record and remove name from the queue
            if(file!=null){
                playSound(file);                                //  Play the file, if the String is not null.
            }
        }
    }

    //  Plays the sound file specified by creating a new AudioStream around it.
    public void playSound(String soundFileName) {
        try {
            as = new AudioStream(new FileInputStream(System.getProperty("user.dir") + "/sounds/" + soundFileName));
            AudioPlayer.player.start(as);
        } catch (FileNotFoundException fe) {
            System.out.println(fe);
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }

}
