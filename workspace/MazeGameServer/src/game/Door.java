package game;

import engine.Position;
/*
* Classname:            Door.java
*
* Version information:  1.0
*
* Date:                 11/3/2013
*
* Copyright notice:     Copyright (c) 2013 Garrett Benoit
*/

/**
 * EnvironmentTile: Level background tile
 */
public class Door extends Entity {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 4081052719686701412L;
    public static final int TILESIZE = 16;
    private Door linkedDoor;
    private Room room;
    private Position<Integer, Integer> exitLocation;
    public static enum Side {TOP, LEFT, RIGHT, BOTTOM};
    private Side side;
    private boolean locked = false; // Implement later
    

    /**
     * Constructor
     * @param g
     * @param anImage
     * @param x
     * @param y
     */
    public Door(Game g, String anImage, int x, int y, Position<Integer, Integer> exitLoc, Room room, Door linkedDoor, Side side) {
        super(g,anImage,x,y,TILESIZE+8,TILESIZE+8);
        minX = x-4;
        minY = y-4;
        width = TILESIZE+8;
        height = TILESIZE+8;
        offsetX = -Math.abs(imageX - minX);
        offsetY = -Math.abs(imageY - minY);
        calculateBounds();
        this.room = room;
        this.linkedDoor = linkedDoor;
        if(this.linkedDoor != null) {
            this.linkedDoor.setLink(this);
        }
        exitLocation = exitLoc;
        this.side = side;
    }
    
    public void transport(Entity player) {
        player.setMinX(linkedDoor.getExit().getX());
        player.setMinY(linkedDoor.getExit().getY());
        //room.removePlayer(player); // ignore this for portal, I'll fix this later
        linkedDoor.getRoom().addPlayer(player);
    }
    
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
    
    public Side getSide() {
        return side;
    }
    
    public Position<Integer, Integer> getExit() {
        return exitLocation;
    }
    
    public Room getRoom() {
        return room;
    }
    
    public Door getLink() {
        return linkedDoor;
    }
    
    public void setLink(Door linkedDoor) {
        this.linkedDoor = linkedDoor;
    }
}
