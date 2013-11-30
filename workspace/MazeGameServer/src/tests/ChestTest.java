package tests;

/*
* Classname:            ChestTest.java
*
* Version information:  1.0
*
* Date:                 11/8/2013
*
* Copyright notice:     Copyright (c) 2013 Garrett Benoit
*/

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import game.Game;
import game.GameEngine;
import game.MazeGameServer;
import game.entities.environment.Chest;

import org.junit.Test;

/**
 * JUnit tests for the Chest class. These tests will not include anything from the superclass Entity.
 * ChestTest: <add description>
 */
public class ChestTest {
    
    public Game game; // just a temporary game object to perform our tests on
    public String image;
    public int xPos;
    public int yPos;
    public Chest chest;
    
    public void main(String [] args) {
    }
    
    @Test
    public void testChestConstructor() {
        initiateTestVariables();
        // linked Door is an optional parameter, so test with no linked door
        chest = null;
        chest = new Chest(game, image, xPos, yPos);
        assertNotNull(this.chest);
    }
    
    @Test
    public void testGenerateChestContents() {
        initiateTestVariables();
        chest = null;
        chest = new Chest(game, image, xPos, yPos);
        assertNotNull(chest.getContents());
        chest.generateContents();
        assertNotNull(chest.getContents());
    }
    
    @Test
    public void testDropChestContentsAndLocks() {
        initiateTestVariables();
        chest = null;
        chest = new Chest(game, image, xPos, yPos);
        chest.lock();
        assertEquals(chest.dropContents(), false);
        chest.unlock();
        assertEquals(chest.dropContents(), true);
    }
    
    public void initiateTestVariables() {
        try {
            game = new MazeGameServer(new GameEngine());
            image = "chestLockedImage";
            xPos = 232;
            yPos = 72;
        } catch(Exception e) {
            System.out.println("Variable instantiation failed. Aborting JUnit tests.");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
