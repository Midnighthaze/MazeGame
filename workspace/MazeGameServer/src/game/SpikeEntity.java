package game;
import java.util.Random;

/*
* Classname:            SpikeEntity.java
*
* Version information:  1.0
*
* Date:                 10/30/2013
*
* Copyright notice:     Copyright (c) 2013 Garrett Benoit
*/


/**
 * SpikeEntity: spike traps
 */
public class SpikeEntity extends Entity {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -4363795480195834006L;
    MazeGameServer game;
    private float direction = 0;
    private long directionTime = 0;
    private String imageArray[] = {"spikeFloor.gif","spikeCeiling.gif","spikeLeft.gif","spikeRight.gif"};
    private int imageIndex = 0;
    private boolean facingRight = true;
    private static final int COLLISION_DAMAGE = 10;
    
    public SpikeEntity(Game g, String img, int iX, int iY, int x, int y, int w, int h) {
        super(g, img, iX, iY, w, h);
        game = (MazeGameServer) g;
        minX = x;
        minY = y;
        width = w;
        height = h;
        offsetX = Math.abs(imageX - minX);
        offsetY = Math.abs(imageY - minY);
        calculateBounds();
        setHealthPoints(0);
        damage = COLLISION_DAMAGE;
    }
}