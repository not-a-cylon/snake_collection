import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Created by Swayze on 9/5/2016.
 */
public class CustomAction extends AbstractAction {

    public String keyPress;
    public GUIDirector guiReference;

    public CustomAction(String targetKey){
        super();
        keyPress = targetKey;
    }

    public void setGUIDirectorReference(GUIDirector gui){
        guiReference = gui;
    }

    public void actionPerformed(ActionEvent e) {
        guiReference.replaceAction(keyPress);
    }

}
