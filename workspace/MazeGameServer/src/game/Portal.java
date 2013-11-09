package game;

import java.util.ArrayList;
import java.util.Random;
import engine.Position;
/*
* Classname:            Portal.java
*
* Version information:  1.0
*
* Date:                 11/6/2013
*
* Copyright notice:     Copyright (c) 2013 Lizhu Ma & Qiaoli Hu
*/


/**
 * EnvironmentTile: Level background tile
 */
public class Portal extends Entity {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 4081052719686701412L;
    public static final int TILESIZE = 16;
    private ArrayList<Room> rooms; 
    private Room myRoom;
    private Room linkedRoom;
    public static enum Side {TOP, LEFT, RIGHT, BOTTOM};
    private boolean activated = false;
    

    /**
     * Constructor
     * @param g - This is the game instance, will be removed later (forced to have from entity).
     * @param anImage - Which image we'd like to render
     * @param x - the starting location x
     * @param y - the starting location y
     */
    public Portal(Game g, String anImage, int x, int y, Room myRoom, ArrayList<Room> rooms) {
        super(g,anImage,x,y,TILESIZE+8,TILESIZE+8);
        minX = x-4;
        minY = y-4;
        width = TILESIZE+8;
        height = TILESIZE+8;
        offsetX = -Math.abs(imageX - minX);
        offsetY = -Math.abs(imageY - minY);
        calculateBounds();
        this.myRoom = myRoom;
        this.rooms = rooms;
        activated = false;
  
    }
    
    @Override
    public void update(long time) {
        // Animate the idle animation for
        // an enabled portal
        
        // Don't worry about this for now,
        // I'll try to create an Animator class
        // or you can try to code it like how it's done
        // in in the Player class.
        
        // The only problem is we have no animations
        // to use for this unless you just use
        // some of the assets we already have.
    }
    
    // Get this from Door
    public void transport(Entity player) {
        // if enabled
            // pick random room that is not myRoom
            // set players coordinates to that room's getCenter
            // add player to that room
        int rnd = new Random().nextInt(rooms.size());
        
        if (isActivated())
        {
            linkedRoom = rooms.get(rnd);
            player.setMinX(linkedRoom.getCenter().getX());
            player.setMinY(linkedRoom.getCenter().getY());
            //linkedRoom.removePlayer(player); // ignore this for portal, I'll fix this later
            linkedRoom.addPlayer(player);
        }
        //for testing purpose
        //deactivate();
    }
    
    // Get this from Door
    public boolean contains(Entity player) {
        if(player.getMaxX() >= this.getMaxX())
            return false;
        if(player.getMinX() <=  this.getMinX())
            return false;
        if(player.getMaxY() >=  this.getMaxY())
            return false;
        if(player.getMinY() <=  this.getMinY())
            return false;
        return true;
    }
    
    public void activate() {
        activated = true;
    }
    
    public void deactivate() {
        activated = false;
    }
    
    public boolean isActivated() {
        return activated;
    }
    
    public Room getRoom() {
        return myRoom;
    }
    public Room getLinkedRoom() {
        return linkedRoom;
    }
}