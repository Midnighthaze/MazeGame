package game;
import items.Item;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

import engine.Vertex2;
import engine.physics.*;
import engine.serializable.SerializedObject;
import engine.serializable.SerializedRoom;
import game.levelloader.Level;
import game.levelloader.LevelLoader;


/*
* Classname:            MazeGameServer.java
*
* Version information:  1.0
*
* Date:                 10/30/2013
*
* Copyright notice:     Copyright (c) 2013 Garrett Benoit
*/

/**
 * MazeGameServer: This is our Mega Man game
 */
public class MazeGameServer extends Game {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -3118971393018891785L;
    ArrayList<ArrayList<Boolean>> inputs = null;
    //private int theWidth = 1024; // could be used if gotten from client to resize sprites
    //private int theHeight = 768;
    //private ArrayList<Entity> healthbar = null;
    Level level;
    
    public static enum Sound {
        HIT(0), SHOT(1), DEFLECT(2), SPAWN(3), DEAD(4), MUSIC(5);
        private final int value;
        private Sound(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
    };
    
    /**
     * Constructor
     * 
     * @param e
     */
    public MazeGameServer(GameEngine e) throws Exception {
        super(e);
        //healthbar = new ArrayList<Entity>();
        // TEMPORARY
        LevelLoader.game = this;
    }
    
    @Override
    public ArrayList<Entity> getEntities() {
        ArrayList<Entity> tmp = new ArrayList<Entity>();
        return tmp;
    }

    public ArrayList<ArrayList<Boolean>> initInputs() {
        level = LevelLoader.generateRandomLevel(LevelLoader.LevelSize.SMALL);
        inputs = new ArrayList<ArrayList<Boolean>>();
        for(int i = 0; i < 4; i++) { // tmp to allow multiplayer
            ArrayList<Boolean> tmpInputs = new ArrayList<Boolean>();
            tmpInputs.add(false); //inputs.add(right);
            tmpInputs.add(false); //inputs.add(left);
            tmpInputs.add(false); //inputs.add(up);
            tmpInputs.add(false); //inputs.add(down);
            tmpInputs.add(false); //inputs.add(fire);
            tmpInputs.add(false); //inputs.add(escape);
            tmpInputs.add(false); //inputs.add(pause);
            tmpInputs.add(false); //inputs.add(startGame);
            tmpInputs.add(false);//input.forward
            tmpInputs.add(false);//input.backward
            inputs.add(tmpInputs);
        }
        /*for(int i = 0; i < 29; i++) {
            healthbar.add(new HealthBar(this, "\\healthbar\\health"+i+".gif", players.get(0)));
        }*/
        //initSounds();
        return inputs;
    }
    
    private void initSounds() {
        /*sound_hit = GameEngine.addSound("hit.wav");
        sound_shot = GameEngine.addSound("shot.wav");
        sound_spawn = GameEngine.addSound("spawn.wav");
        sound_deflect = GameEngine.addSound("deflect.wav");
        sound_dead = GameEngine.addSound("dead.wav");
        BGM_quickman = GameEngine.addSound("music/quickmanBGM.wav");*/
    }
    
    /*
     * (non-Javadoc)
     * @see IGame#update(long)
     */
    @Override
    public List<SerializedObject> update(long time) {
        List<SerializedObject> generatedUpdates = new ArrayList<SerializedObject>();
        spawnPlayer(time);
        
        /*if((GameEngine.getTime()-timeBGM) > 38000) {
            timeBGM = GameEngine.getTime();
        }*/
        
        Exterior ext = level.getExterior();
        if(ext.numPlayers() > 0) {
            for(Entity p: ext.getPlayers()) {
                if(p.needsDelete()) {
                    if(p.getLives() > 0) {
                        ((Player) p).reset();
                        p.calculateBounds();
                        p.setLives(p.getLives()-1);
                    }
                    else {
                        // LOSE
                    }
                }
                p.update(time);
                generatedUpdates.add(p.serialize());
                for(Entity shot: p.getShots()) {
                    generatedUpdates.add(shot.serialize());
                }
            }
            
            for(Door d: ext.getDoors()) {
                int i = 0;
                while(i < ext.getPlayers().size()) {
                    if(d.contains(ext.getPlayers().get(i))) {
                        d.transport(ext.getPlayers().get(i));
                        ext.removePlayer(ext.getPlayers().get(i));
                    } else i++;
                }
                generatedUpdates.add(d.serialize());
            }
            //healthbar.get(players.get(0).getHealthPoints()).update(time);
            //generatedUpdates.add(healthbar.get(players.get(0).getHealthPoints()).serialize());
            generatedUpdates.add(new SerializedRoom(null, 0));
            checkTileCollision(ext);
        }
        
        for(int room = 0; room < level.getRooms().size(); room++) {
            Interior r = level.getRooms().get(room);
            if(r.numPlayers() > 0) {
                generatedUpdates.add(new SerializedRoom(r.getLocation(), room+1));
                for(Entity p: r.getPlayers()) {
                    if(p.needsDelete()) {
                        if(p.getLives() > 0) {
                            ((Player) p).reset();
                            p.calculateBounds();
                            p.setLives(p.getLives()-1);
                        }
                        else {
                            // LOSE
                        }
                    }
                    p.update(time);
                    generatedUpdates.add(p.serialize());
                    for(Entity shot: p.getShots()) {
                        generatedUpdates.add(shot.serialize());
                    }
                }
                
                for(Door d: r.getDoors()) {
                    int i = 0;
                    while(i < r.getPlayers().size()) {
                        if(d.contains(r.getPlayers().get(i))) {
                            d.transport(r.getPlayers().get(i));
                            r.removePlayer(r.getPlayers().get(i));
                        } else i++;
                    }
                    generatedUpdates.add(d.serialize());
                }
                
                for(Portal p: r.getPortals()) {
                    int i = 0;
                    while(i < r.getPlayers().size()) {
                        if(p.isActivated() && p.contains(r.getPlayers().get(i))) {
                            p.transport(r.getPlayers().get(i));
                            r.removePlayer(r.getPlayers().get(i));
                        } else i++;
                    }
                    generatedUpdates.add(p.serialize());
                }
                
                //healthbar.get(players.get(0).getHealthPoints()).update(time);
                //generatedUpdates.add(healthbar.get(players.get(0).getHealthPoints()).serialize());
                
                int i = 0;
                while(i < r.getEnemies().size()) {
                    if(r.getEnemies().get(i).needsDelete()) {
                        r.generateRandomItems(this, (int) r.getEnemies().get(i).getMidX(), (int) r.getEnemies().get(i).getMidY());//cannot pass game parameter
                        r.getEnemies().remove(i);
                    }
                    else {
                        r.getEnemies().get(i).update(time);
                        generatedUpdates.add(r.getEnemies().get(i).serialize());
                        for(Entity shot: r.getEnemies().get(i).getShots()) {
                            generatedUpdates.add(shot.serialize());
                        }
                        i++;
					}
				}

                int j = 0;
                while(j < r.getItems().size()) {
                    if(r.getItems().get(j).needsDelete()) {
                        r.getItems().remove(j);
                    }
                    else {
                        //System.out.print("item:"+r.getEnemies().size());
                        r.getItems().get(j).update(time);
                        generatedUpdates.add(r.getItems().get(j).serialize());
                        
                        j++;
                    }
                }
        
                checkCollisions(generatedUpdates, r);
                checkTileCollision(r);
            }
        }
        return generatedUpdates;
    }

    // THIS FUNCTION WILL BE DELETED, SHOULDNT EXIST
    private void spawnPlayer(long time) {
        Room r = level.getExterior();
        //for(Interior r: level.getRooms()) {
            if(r.numPlayers() > 0) {
                for(Entity p: r.getPlayers()) {
                    ((Player) p).nextAnimation("spawnArray",6);
                }
            }
        //}
    }
  

    private void checkCollisions(List<SerializedObject> generatedUpdates, Interior room) {
        for (Entity player: room.getPlayers()) {
            for(Entity enemy: room.getEnemies()) {
                if(Collisions.detectCollision(enemy, player)) {
                    player.takeDamage(enemy.getDamage());
                }
                for(Entity shot: player.getShots()) {
                    if(Collisions.detectCollision(enemy, shot)) {
                        ((ShotEntity) shot).bulletHit(enemy);
                    }
                }
                for(Entity shot: enemy.getShots()) {
                    if(Collisions.detectCollision(shot, player)) {
                        ((ShotEntity) shot).bulletHit(player);
                    }
                }
            }

            for(Entity item: room.getItems()) {
                if(Collisions.detectCollision(item, player)) {
                    //player.takeDamage(item.getDamage());
                }
                
            }
            for(GateKeeper gateKeeper: room.getGateKeepers()){
                if(Collisions.detectCollision(player, gateKeeper)){
                    gateKeeper.negotiate(player);
                    ArrayList<PenetrationData<Collisions.Position, Float, Float>> pen = new ArrayList<PenetrationData<Collisions.Position, Float, Float>>();
                    pen.add(Collisions.calculatePenetration(player, gateKeeper));
                    if(pen.size() > 0) Collisions.applyPenetrationCorrections(player, pen);
                }
                generatedUpdates.add(gateKeeper.serialize());
            }
            for(Portal portal: room.getPortals()){
               if(Collisions.detectCollision(player, portal)){
                   if(!portal.isActivated()){
                       ArrayList<PenetrationData<Collisions.Position, Float, Float>> pen = new ArrayList<PenetrationData<Collisions.Position, Float, Float>>();
                       pen.add(Collisions.calculatePenetration(player, portal));
                       if(pen.size() > 0) Collisions.applyPenetrationCorrections(player, pen);
                   }
               }
           }
            for(int i = 0; i< room.getItems().size(); i++){
                if(Collisions.detectCollision(player, room.getItems().get(i))){
                    Player p = (Player) player;
                    p.pickItem(room.getItems().get(i));
                    room.removeItem(room.getItems().get(i));
                }
            }
        }
        
            for(Entity trap: room.getTraps()) {
            for(Entity player: room.getPlayers()) {
                if(Collisions.detectCollision(trap, player)) {
                    player.takeDamage(trap.getDamage());
                }
            }
        }
    }

    // checks gravity and collision of all entities vs the world environment
    private void checkTileCollision(Room room) {
        for(Entity player: room.getPlayers()) {
            Collisions.applyEnvironmentCollision(player, room.getForeground());
        }
        
        if(room instanceof Interior) {
            for(Entity enemy: ((Interior) room).getEnemies()) {
                Collisions.applyEnvironmentCollision(enemy, room.getForeground());
            }
            
        }
    }
}

