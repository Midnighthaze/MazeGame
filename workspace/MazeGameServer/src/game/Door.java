package game;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import engine.Vertex2;
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
    private Vertex2 exitLocation;
    public static enum Side {
        TOP("top",0), LEFT("left",1), RIGHT("right",2), BOTTOM("bottom",3);
        private static final Side[] VALUES = values();
        private static final int SIZE = VALUES.length;
        private final String value;
        private final int index;
        private Side(String value, int index) {
            this.value = value;
            this.index = index;
        }
        public static Side findByValue(String value) {
            for(int i = 0; i < SIZE; i++) {
                if(VALUES[i].getValue().equalsIgnoreCase(value)) return VALUES[i];
            }
            return null;
        }
        public Side opposite() {
            if(this.equals(TOP)) return BOTTOM;
            else if(this.equals(BOTTOM)) return TOP;
            else if(this.equals(RIGHT)) return LEFT;
            else if(this.equals(LEFT)) return RIGHT;
            return null;
        }
        public int getIndex() {
            return index;
        }
        public String getValue() {
            return value;
        }
    };
    private Side side;
    private boolean locked = false; // Implement later
    

    /**
     * Constructor
     * @param g
     * @param anImage
     * @param x
     * @param y
     */
    public Door(Game g, String anImage, int x, int y, Vertex2 exitLoc, Room room, Door linkedDoor, Side side) {
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
    
    public Vertex2 getExit() {
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
        this.linkedDoor.linkedDoor = this;
    }
}
