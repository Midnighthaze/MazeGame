package game;
import java.util.ArrayList;
import java.util.Random;

import engine.physics.Collisions;
import engine.serializable.SerializedObject;

/*
* Classname:            WoodManEntity.java
*
* Version information:  1.0
*
* Date:                 10/30/2013
*
* Copyright notice:     Copyright (c) 2013 Garrett Benoit
*/

/**
 * ShieldGuyEntity: Shield Guy entity is used for Shield Guy game.
 * Each Shield Guy entity has a direction and direction time for moving the
 * entity.
 */
public class WoodManEntity extends Entity {
    MazeGameServer game;
    private float direction = 0;
    private long directionTime = 0;
    private String imageArray[] = {"\\enemies\\woodman1.gif","\\enemies\\woodman2.gif"};
    private String imageArrayRight[] = {"\\enemies\\woodman1Right.gif","\\enemies\\woodman2Right.gif"};
    private String dieArray[] = {"\\enemies\\woodmanDeath1.gif","\\enemies\\woodmanDeath2.gif","\\enemies\\woodmanDeath3.gif","damage3.gif","damage4.gif"};
    private String dieArrayRight[] = {"\\enemies\\woodmanDeathRight1.gif","\\enemies\\woodmanDeathRight2.gif","\\enemies\\woodmanDeathRight3.gif","damage3.gif","damage4.gif"};
    private int imageIndex = 0;
    private int numBullets;
    private boolean isJump = false;
    private boolean spawned = false;
    private boolean moveRight = false;
    private boolean moveLeft = false;
    private boolean isShoot = false;
    private boolean facingRight = true;
    private boolean isFire = false;
    private float moveFactor = 10.0f;
    private float speed = 1.5f;
    private int shotTimer = 0;
    private float lastShotTime;
    private static final int MAX_HEALTH = 20;
    private static final int LEAF_DAMAGE = 5;
    private static final int COLLISION_DAMAGE = 5;
    private static final int AGGRO_RANGE = 192;
    private Room room;
    private int isDying = 0;
    private long currentTime = 0;
    
    public WoodManEntity(Game g, String file, int iX, int iY, int x, int y, int w, int h, Room room) {
        super(g, file, iX, iY, w, h);
        game = (MazeGameServer) g;
        imageIndex = imageIndex + 10; 
        image = imageArray[0];
        minX = x;
        minY = y;
        width = w;
        height = h;
        offsetX = Math.abs(imageX - minX);
        offsetY = Math.abs(imageY - minY);
        lastShotTime = 0;
        calculateBounds();
        setHealthPoints(MAX_HEALTH);
        isDead = false;
        damage = COLLISION_DAMAGE;
        this.room = room;
        numBullets = 0;
    }
    
    /*public void nextAnimation(String arrayName, int frames) {  
        if(arrayName.equals("spawnArray") && spawned == false){
            imageIndex = imageIndex + 20; 
            if(imageIndex >= frames*100) {
                imageIndex = 0;
            }
            int test = imageIndex/100;
            image = imageArray[test];
            if(image.equals("standingRight.gif")){
                spawned = true;
            }
        }
    }*/
    
    /*public void jump(){
        imageIndex = imageIndex + 10; 
        image = "shieldguyJump.gif";
    }*/

    public void setIsJump(boolean isJump) {
        this.isJump = isJump;
    }

    public boolean getIsJump() {
        return isJump;
    }
    
    /**
     * fire: shoot a bullet
     */
    public void fire(boolean isRight) {
        //GameEngine.playSound(game.sound_shot);
        if(isRight) {
            shots.add(new ShotEntity(game, "\\enemies\\woodmanLeaf1.gif",(int) maxX,
                    (int) minY+8, Face.RIGHT, LEAF_DAMAGE, AGGRO_RANGE));
        }
        else if(!isRight) {
            shots.add(new ShotEntity(game, "\\enemies\\woodmanLeaf4.gif",(int) minX,
                    (int) minY+8, Face.LEFT, LEAF_DAMAGE, AGGRO_RANGE));
        }
        lastShotTime = GameEngine.getTime();
    }
    
    @Override
    public void takeDamage(int d) {
        setHealthPoints(getHealthPoints()-d);
        //GameEngine.playSound(((MazeGameServer)game).sound_hit);
        if(getHealthPoints() == 0 && isDying == 0) {
            isDead = true;
            imageIndex = 0;
            deathAnimate();
        }
    }
    
    @Override
    public void update(long time) {
        movements(time);
        
        int i = 0;
        while(i < shots.size()) {
            if(shots.get(i).needsDelete()) {
                shots.remove(i);
                numBullets--;
            }
            else {
                shots.get(i).update(time);
                i++;
            }
        }
    }
    
    public void deathAnimate() {
            isDying = 1;
            currentTime = GameEngine.getTime();
    }
    
    public void movements(long time) {
        if(!isDead) {
            // find closest player
            Entity player = null;
            float minDist = 99999; // tmp replace later
            for(Entity p: room.getPlayers()) {
                float dist = Collisions.findDistance(p, this);
                if(dist < minDist) {
                    minDist = dist;
                    player = p;
                }
            }
            if(player == null) return;
            if(Math.abs(player.getMidX()-midX) < AGGRO_RANGE && Math.abs(player.getMidY()-midY) < AGGRO_RANGE) {
                if(player.getMidX()-midX > 0) {
                    facingRight = true;
                }
                else {
                    facingRight = false;
                }
                if(true/*!isFalling*/) {/*
                    if (player.getIsJumping() && !isJump) {
                        velY = 3;
                        if(facingRight) {
                            velX = 1;
                        }
                        else {
                            velX = -1;
                        }
                        move((int) (velX * (time / moveFactor)), -(int) (velY * (time / moveFactor)));
                        setIsFalling(true);
                        isJump = true;
                    }
                    else if(!player.getIsJumping() && isJump) {
                        isJump = false;
                    }*/
                    if(facingRight) {
                        velX = 1;
                    }
                    else {
                        velX = -1;
                    }
                    move((int) (velX * (time / moveFactor)), -(int) (velY * (time / moveFactor)));
                }
                Random generator = new Random( GameEngine.getTime() );
                int rand = generator.nextInt() % 100;
                if ((rand >= 80 && numBullets <= 0) || GameEngine.getTime() - lastShotTime > 2000) {
                    if(GameEngine.getTime() - lastShotTime > 200){
                        fire(facingRight);
                        numBullets++;
                    }
                    isFire = true;
                    shotTimer = 0;
                }
            }

            imageIndex = imageIndex + 10;
            if(imageIndex >= 200) {
                imageIndex = 0;
            }
            if(player.getMidX()-midX > 0) {
                facingRight = true;
                if(shotTimer >= 15) {
                    image = imageArrayRight[imageIndex/100];     
                }
            }
            else {
                facingRight = false;
                if(shotTimer >= 15) {
                    image = imageArray[imageIndex/100];
                }
            }/*
            if(isFalling) {
                velY = velY - (gravity*(time/1000.0f));
                move((int) (velX * (time / moveFactor)), -(int) (velY * (time / moveFactor)));
                //image = "shieldguyJump.gif";
            }
            if(isFalling && isJump) {
                if(facingRight && shotTimer >= 15) {
                    image = "\\enemies\\woodman3Right.gif";
                }
                else if(!facingRight && shotTimer >= 15) {
                    image = "\\enemies\\woodman3.gif";
                }
            }*/
            if(isFire){
                isFire = false;
                if(facingRight) {
                    image = "\\enemies\\woodman4Right.gif";
                }
                else if(!facingRight) {
                    image = "\\enemies\\woodman4.gif";
                }
            }
            shotTimer++;
        }
        else {
            long seconds = GameEngine.getTime() - currentTime;
            if(seconds > (1333/6)){
                isDying++;
                currentTime = GameEngine.getTime();
            }
            imageIndex = imageIndex + 10; 
            if(imageIndex >= 400) {
                imageIndex = 0;
            }
            if(facingRight) {
                image = dieArrayRight[imageIndex/100];
            }
            else if(!facingRight) {
                image = dieArray[imageIndex/100];
            }
            
            if(isDying >= 4){
                remove = true;
                game.setWin(true);
            }
        }
    }
}