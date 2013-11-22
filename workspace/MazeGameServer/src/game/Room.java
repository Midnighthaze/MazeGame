package game;

/*
* Classname:            Room.java
*
* Version information:  1.0
*
* Date:                 11/3/2013
*
* Copyright notice:     Copyright (c) 2013 Garrett Benoit
*/

import game.levelloader.LevelLoader.EntryType;
import items.Item;

import java.util.ArrayList;

import engine.Vertex2;
import engine.serializable.SerializedObject;
import engine.serializable.SerializedRoom;

public class Room {
    private ArrayList<Entity> foreground = new ArrayList<Entity>();
    //private ArrayList<Entity> background = new ArrayList<Entity>();
    private ArrayList<Entity> players = new ArrayList<Entity>();
    private ArrayList<Door> doors = new ArrayList<Door>();
    private ArrayList<Item> items = new ArrayList<Item>();
    
    public final int layout; // temporary
    
    public Room(int layout) {
        this.layout = layout;
    }
    
    public void addToForeground(Entity tile) {
        foreground.add(tile);
    }
    /*public void addToBackground(Entity tile) {
        background.add(tile);
    }*/
    
    public void addPlayer(Entity player) {
        players.add(player);
    }
    
    public void removePlayer(Entity player) {
        players.remove(player);
    }
    
    public void addDoor(Door door) {
        doors.add(door);
    }
    
    public int numPlayers() {
        return players.size();
    }
    
    public ArrayList<Entity> getPlayers() {
        return players;
    }
    
    /*public ArrayList<Entity> getBackground() {
        return background;
    }*/
    
    public ArrayList<Entity> getForeground() {
        return foreground;
    }
    
    public ArrayList<Door> getDoors() {
        return doors;
    }
    
    public SerializedObject serialize(int index) {
        return new SerializedRoom(null, index);
    }
}
