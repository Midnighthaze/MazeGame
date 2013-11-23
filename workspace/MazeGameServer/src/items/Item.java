package items;

import game.Entity;
import game.Game;
import game.MazeGameServer;
import game.Player;

/*
* Classname:            Item.java
*
* Version information:  1.0
*
* Date:                 11/17/2013
*
* Copyright notice:     Copyright (c) 2013 Lizhu Ma
*/

public abstract class Item extends Entity{

    //public enum ItemType {HEALTHBOOSTER, WEAPON, KEY};
    //public ItemType type;
    
    public Item(Game g, String anImage, int x, int y, float w, float h) {
        super(g, anImage, x, y, w, h);
        calculateBounds();
    }


    
}
    
    
    



