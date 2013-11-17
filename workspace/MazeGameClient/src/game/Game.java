package game;
import java.util.ArrayList;
import java.util.List;

import engine.inputhandler.Input;
import engine.render.IDisplay;
import engine.serializable.SerializedObject;
import engine.serializable.SerializedRoom;

/*
* Classname:            Game.java
*
* Version information:  1.0
*
* Date:                 10/30/2013
*
* Copyright notice:     Copyright (c) 2013 Garrett Benoit
*/
/**
 * IGame: Game interface
 */
public abstract class Game {
    protected transient GameEngine engine;
    protected boolean isDone;
    private transient IDisplay theDisplay;
    
    public Game(GameEngine e) {
        engine = e;
        isDone = false;
    }
    
    /**
     * isDone: Return is done; true if the game is done (time to exit);
     * false otherwise.
     * 
     * @return isDone
     */
    public boolean isDone() {
        return isDone;
    }
    
    /**
     * init: initializes the game and returns a list of the inputs the
     * game is interested in
     * @param levelLayout 
     * 
     * @return a list of the inputs the game will use
     */
    public ArrayList<Input> initInputs(List<SerializedRoom> levelLayout) {
        return null;
    }
    
    public void shutdown() {
      
    }
    
    /**
     * update: updates the Game's world.
     * 
     * @param time
     *            <add description>
     */
    public void update(long time, List<SerializedObject> updateObjects) {
        
    }
    
    /**
     * getDrawables: Returns a list of the entities in the game.
     * 
     * @return
     */
    public ArrayList<Entity> getEntities() {
        return null;
    }
    
    public IDisplay getDisplay() {
        return theDisplay;
    }
    
    public void setDisplay(IDisplay aDisplay) throws Exception {
        if (aDisplay == null) {
            throw new Exception("Display is null");
        }
        theDisplay = aDisplay;
    }
}
