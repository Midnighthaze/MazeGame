package game.entities.environment;

import engine.Vector2f;
import engine.physics.RigidBody;
import engine.serializable.SerializedObject;
import engine.serializable.SerializedObstacle;
import game.entities.Entity;
import game.entities.npcs.Player;
import game.enums.AnimationPath;
import game.enums.Face;
import game.enums.Side;
import game.environment.Room;

public abstract class Entry extends Entity {
    protected Room room;
    protected Side side;
    
    public Entry(AnimationPath ap, RigidBody rb, Room room, Side side) {
        super(ap, rb);
        this.room = room;
        this.side = side;
        switch(this.side) {
            case RIGHT:
                facing = Face.LEFT;
                break;
            case LEFT:
                facing = Face.RIGHT;
                break;
            case TOP:
                facing = Face.DOWN;
                break;
            case BOTTOM:
                facing = Face.UP;
                break;
        }
    }
    
    public Room getRoom() {
        return room;
    }
    
    public Side getSide() {
        return side;
    }
    
    public void update(long elapsedTime) {
        // do nothing
    }
    
    public abstract boolean transport(Player player);
    
    public boolean interact(Player player) {
        return false;
    }
    
    protected boolean contains(Player player) {
        if(player.getRigidBody().getMax().x >= rBody.getMax().x)
            return false;
        if(player.getRigidBody().getMin().x <= rBody.getMin().x)
            return false;
        if(player.getRigidBody().getMax().y >= rBody.getMax().y)
            return false;
        if(player.getRigidBody().getMin().y <= rBody.getMin().y)
            return false;
        return true;
    }
    
    @Override
    public SerializedObject serialize() {
        return new SerializedObstacle(uuid, animPath, animState, facing, new Vector2f(rBody.getLocation()), !isEnabled());
    }
}
