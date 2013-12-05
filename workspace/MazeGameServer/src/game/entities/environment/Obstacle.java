package game.entities.environment;

import engine.Vector2f;
import engine.physics.RigidBody;
import engine.serializable.SerializedObject;
import engine.serializable.SerializedObstacle;
import game.entities.Entity;
import game.entities.npcs.Hostile;
import game.entities.npcs.Player;
import game.enums.AnimationPath;

/*
* Classname:            Obstacle.java
*
* Version information:  1.0
*
* Date:                 10/30/2013
*
* Copyright notice:     Copyright (c) 2013 Garrett Benoit
*/

/**
 * Obstacle: can consist of damaging traps (i.e. spikes) or destructable rocks or objects in the way in general
 */
public abstract class Obstacle extends Entity {
    public static final int COLLISION_DAMAGE = 10;
    protected boolean destructable = false;
    protected boolean dangerous = false;
    protected boolean blocking = false;
    protected boolean openable = false;
    protected boolean moveable = false;
    
    public Obstacle(AnimationPath ap, RigidBody rb) {
        super(ap, rb);
    }
    
    public void update(long elapsedTime) {
        // do nothing
    }
    
    public void collide(Hostile hostile) {
        hostile.takeDamage(COLLISION_DAMAGE);
    }
    
    public void interact(Player player) {
        
    }
    
    public void destroy() {
        if(destructable) {
            disable();
        }
    }
    
    public boolean isMoveable(){
        return moveable;
    }
    
    public boolean isOpenable() {
        return openable;
    }
    
    public boolean isDestructable() {
        return destructable;
    }
    
    public boolean isDangerous() {
        return dangerous;
    }
    
    public boolean isBlocking() {
        return blocking;
    }
    
    public boolean isMoveable() {
        return moveable;
    }
    
    @Override
    public SerializedObject serialize() {
        return new SerializedObstacle(uuid, animPath, animState, facing, new Vector2f(rBody.getLocation()), !isEnabled());
    }
}