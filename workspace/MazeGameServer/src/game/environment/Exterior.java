package game.environment;

/*
* Classname:            Exterior.java
*
* Version information:  1.0
*
* Date:                 11/12/2013
*
* Copyright notice:     Copyright (c) 2013 Garrett Benoit
*/

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import engine.Vector2f;
import engine.Vector2i;
import engine.physics.Collisions;
import engine.serializable.SerializeFactory;
import engine.serializable.SerializedObject;
import game.MazeGameServer;
import game.entities.environment.Entry;
import game.entities.environment.Explosion;
import game.entities.environment.Tile;
import game.entities.items.ABomb;
import game.entities.items.Item;
import game.entities.npcs.Neutral;
import game.entities.npcs.Player;
import game.entities.projectiles.Projectile;
import game.enums.GameState;

public class Exterior extends Room{
    public static final int HEIGHT = 528;
    public static final int WIDTH = 720;
    public static final int SPAWN_RADIUS = 50;
    private ArrayList<Vector2i> playerSpawns = new ArrayList<Vector2i>(); // for now make these the safe zones too possibly
    
    public Exterior(int layout) {
        super(layout);
    }
    
    @Override
    public void update(long elapsedTime) {
        if(hasPlayers()) {
            // players
            Iterator<Player> playerItr = players.iterator();
            while(playerItr.hasNext()) {
                Player player = playerItr.next();
                if(player.isEnabled()) {
                    player.update(elapsedTime);
                } else {
                    if(player.getLives() > 0) {
                        player.reset();
                        player.removeLife();
                        player.update(elapsedTime);
                    }
                    else {
                        MazeGameServer.states.set(player.getPlayerID(), GameState.LOSE);
                        playerItr.remove();
                        continue;
                    }
                }
                
                for(Entry entry: entries) {
                    if(entry.transport(player)) {
                        if(player.hasFollower()) {
                            this.removeNeutral(player.getFollower());
                            player.getRoom().addNeutral(player.getFollower());
                            player.getFollower().getRigidBody().setLocation(player.getRigidBody().getLocation());
                        }
                        playerItr.remove();
                        continue;
                    }
                }
                
                if(player.hasFollower()) {
                    if(Collisions.findDistance(new Vector2f(playerSpawns.get(player.getPlayerID())), player.getFollower().getRigidBody().getLocation()) < SPAWN_RADIUS ) {
                        MazeGameServer.hostageSaved(player.getPlayerID());
                        //removeHostage(player.getHostage()); For now only 1 hostage
                    }
                    // In the game update if there's no more hostages to save then player with highest score is winner!
                    // If in single player mode, then level is cleared when all hostages are saved (still get score) same thing
                    // If you lose all lives before game is over then you get -score for that but still +score for each hostage saved
                    // person with highest score wins regardless if they went out
                }
            }
            
            // projectiles
            Iterator<Projectile> projectileItr = projectiles.iterator();
            while(projectileItr.hasNext()) {
                Projectile projectile = projectileItr.next();
                if(projectile.isEnabled()) {
                    projectile.update(elapsedTime);
                } else {
                    projectileItr.remove();
                    continue;
                }
            }
            
            Iterator<Neutral> neutralItr = neutrals.iterator();
            while(neutralItr.hasNext()) {
                Neutral neutral = neutralItr.next();
                if(neutral.isEnabled()) {
                    neutral.update(elapsedTime);
                } else {
                    neutralItr.remove();
                    continue;
                }
            }
            
            // items
            Iterator<Item> itemItr = items.iterator();
            while(itemItr.hasNext()) {
                Item item = itemItr.next();
                if(item.isEnabled()) {
                    item.update(elapsedTime);
                } else {
                    itemItr.remove();
                    continue;
                }
            }
            
            // explosions
            Iterator<Explosion> explosionItr = explosions.iterator();
            while(explosionItr.hasNext()) {
                Explosion explosion = explosionItr.next();
                if(!explosion.isEnabled()) {
                    explosionItr.remove();
                    continue;
                }
            }
        }
    }
    
    @Override
    public void applyCollisions() {
        if(hasPlayers()) {
            // COLLISIONS WITH ENTITES
            // players
            for(Player player: players) {
                if(player.getRigidBody().isEnabled()) {
                    // other players
                    for(Player other: players) {
                        if(!player.equals(other) && other.getRigidBody().isEnabled()) {
                            Collisions.detectAndApplyEqualRadialCorrection(player, other);
                        }
                    }
                    // projectiles
                    for(Projectile projectile: projectiles) {
                        if(projectile.getRigidBody().isEnabled() && projectile.dangerousTo(player) && Collisions.detectCollision(player, projectile)) {
                            projectile.collide(player);
                        }
                    }
                    // neutrals
                    for(Neutral neutral: neutrals) {
                        if(neutral.getRigidBody().isEnabled() && Collisions.detectCollision(player, neutral)) {
                            Collisions.applyEqualRadialCorrection(player, neutral);
                            neutral.interact(player);
                        }
                    }
                    // items
                    Iterator<Item> itemItr = items.iterator();
                    while(itemItr.hasNext()) {
                        Item item = itemItr.next();
                        if(item.getRigidBody().isEnabled() && Collisions.detectCollision(player, item)) {
                            if(item instanceof ABomb){
                                Collisions.applySingleRadialCorrection(item, player);
                            }else{
                                item.pickUp(player);
                            }
                        }
                    }
                    // entries
                    for(Entry entry: entries) {
                        if(entry.getRigidBody().isEnabled()) { // if this is true, it is either a locked door, or a deactivated portal
                            if(!entry.interact(player)) {
                                Collisions.detectAndApplySingleCorrection(player, entry);
                            }
                        }
                    }
                    // explosions
                    for(Explosion explosion: explosions) {
                        if(explosion.getRigidBody().isEnabled()) {
                            Collisions.detectAndApplySingleCorrection(player, explosion);
                            player.takeDamage(explosion.getExplosionDamage(player));
                        }
                    }
                }
            }
            // neutrals
            for(Neutral neutral: neutrals) {
                if(neutral.getRigidBody().isEnabled()) {
                    // items
                    for(Item item: items) {
                        if(item.getRigidBody().isEnabled()) {
                            Collisions.detectAndApplySingleRadialCorrection(item, neutral);
                        }
                    }
                    // entries
                    for(Entry entry: entries) {
                        Collisions.detectAndApplySingleCorrection(neutral, entry);
                    }
                }
            }
            // items
            for(Item item: items) {
                if(item.getRigidBody().isEnabled()) {
                    // entries
                    for(Entry entry: entries) {
                        Collisions.detectAndApplySingleCorrection(item, entry);
                    }
                    
                    // other items
                    for(Item other: items) {
                        if(!item.equals(other) && other.getRigidBody().isEnabled()) {
                            Collisions.detectAndApplyEqualRadialCorrection(item, other);
                        }
                    }
                }
            }
            // COLLISIONS WITH ENVIRONMENT
            // players
            for(Player player: players) {
                if(player.getRigidBody().isEnabled()) {
                    Collisions.applyEnvironmentCorrections(player, foreground);
                }
            }
            // projectiles
            for(Projectile projectile: projectiles) {
                if(projectile.getRigidBody().isEnabled()) {
                    for(Tile tile: foreground) {
                        if(tile.getRigidBody().isEnabled() && Collisions.detectCollision(projectile, tile)) {
                            projectile.collide();
                        }
                    }
                }
            }
            // neutrals
            for(Neutral neutral: neutrals) {
                if(neutral.getRigidBody().isEnabled()) {
                    Collisions.applyEnvironmentCorrections(neutral, foreground);
                }
            }
            // items
            for(Item item: items) {
                if(item.getRigidBody().isEnabled()) {
                    Collisions.applyEnvironmentCorrections(item, foreground);
                }
            }
        }
    }
    
    @Override
    public void serialize() {
        if(hasPlayers()) {
            for(Player player: players) {
                List<SerializedObject> updates = MazeGameServer.updates.get(player.getPlayerID());
                // player
                updates.add(SerializeFactory.serialize(player));
                // room
                updates.add(SerializeFactory.serialize(this));
                // other players
                for(Player other: players) {
                    if(!other.equals(player) && other.isEnabled()) {
                        updates.add(other.serialize());
                    }
                }
                // projectiles
                for(Projectile projectile: projectiles) {
                    if(projectile.isEnabled()) {
                        updates.add(projectile.serialize());
                    }
                }
                // neutrals
                for(Neutral neutral: neutrals) {
                    if(neutral.isEnabled()) {
                        updates.add(neutral.serialize());
                    }
                }
                // items
                for(Item item: items) {
                    if(item.isEnabled()) {
                        updates.add(item.serialize());
                    }
                }
                // entry
                for(Entry entry: entries) {
                    if(entry.isEnabled()) {
                        updates.add(entry.serialize());
                    }
                }
            }
        }
    }
    
    public void addPlayerSpawn(Vector2i location) {
        playerSpawns.add(location);
    }
    
    public ArrayList<Vector2i> getPlayerSpawns() {
        return playerSpawns;
    }
}
