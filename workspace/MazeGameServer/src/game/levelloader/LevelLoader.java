package game.levelloader;

import engine.Vertex2;
import engine.serializable.SerializedRoom;
import game.CannonEntity;
import game.Door;
import game.Door.Side;
import game.Entity;
import game.EnvironmentTile;
import game.Exterior;
import game.GateKeeper;
import game.Interior;
import game.MazeGameServer;
import game.Player;
import game.Portal;
import game.Room;
import game.ShieldGuyEntity;
import game.SpikeEntity;
import game.WoodManEntity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class LevelLoader {
    private final static String GIF = ".gif";
    private final static String layoutPath = "assets/layouts/";
    private final static String exteriorLayout = layoutPath+"exterior/OutsideLayout.oel";
    private final static String roomLayouts[] = {layoutPath+"rooms/RoomLayout0.oel", layoutPath+"rooms/RoomLayout1.oel"};
    private final static String hostageLayout = layoutPath+"rooms/RoomLayout0.oel";//layoutPath+"hostage/HostageRoomLayout.oel";
    private final static String tilesetPath = "tilesets/";
    private final static String animationPath = "animations/";
    
    private static Level level;
    
    private static ArrayList<SerializedRoom> levelLayout = new ArrayList<SerializedRoom>();
    
    public static MazeGameServer game; // TEMPORARY
    
    public static enum LevelSize {
        MINI(3), SMALL(5), MEDIUM(7), LARGE(9);
        private final int value;
        private LevelSize(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
    };
    private static enum EntryType {
        NONE(0.6f), DOOR(0.3f), PORTAL(0.1f);
        private final float probability;
        private EntryType(float probability) {
            this.probability = probability;
        }
        
        public float getProbability() {
            return probability;
        }
    };
    private static enum EnemyType {
        WOOD(animationPath+"woodman/"),
        SHIELD(animationPath+"shieldguy/"),
        CANNON(animationPath+"cannon/");
        
        private final String path;
        private EnemyType(String path) {
            this.path = path;
        }
        public String getPath() {
            return path;
        }
        private static final EnemyType[] VALUES = values();
        private static final int SIZE = VALUES.length;
        private static final Random RANDOM = new Random();
        public static EnemyType randomEnemy() {
            return VALUES[RANDOM.nextInt(SIZE)];
        }
    };
    
    public static ArrayList<SerializedRoom> getLevelLayout() {
        return levelLayout;
    }
    
    public static Level generateRandomLevel(LevelSize breadth) {
        // Create initial variables for level and it's contents
        level = new Level();
        int size = breadth.getValue();
        ArrayList<Interior> rooms = new ArrayList<Interior>();
        for(int i = 0; i < size*size; i++) {
            rooms.add(null);
        }
        EntryType[] generateEntry = new EntryType[Interior.MAX_ENTRIES];
        Vertex2 position = null;
        
        // Create initial room at center of maze with 4 doors
        for(int i = 0; i < generateEntry.length; i++) {
            generateEntry[i] = EntryType.DOOR;
        }
        position = new Vertex2((size/2)*Interior.WIDTH, (size/2)*Interior.HEIGHT);
        rooms.set((size/2)+((size/2)*size), createRoom(0, position, null, generateEntry)); // for now center will be Hostage room
        // Generate Rooms branching outwards from center (only add a room if have a door with no link)
        boolean newLinks = true;
        while(newLinks) {
            newLinks = false;
            for(int room = 0; room < rooms.size(); room++) {
                if(rooms.get(room) != null) {
                    Interior test = rooms.get(room);
                    System.out.println(room + ": " + test.getDoors());
                    for(Door door: rooms.get(room).getDoors()) {
                        if(door.getLink() == null) {
                            int newRoom = -1;
                            if(door.getSide().equals(Side.TOP) && (newRoom = room-size) >= 0) {
                                position = new Vertex2((newRoom%size)*Interior.WIDTH, (newRoom/size)*Interior.HEIGHT);
                                randomEntry(generateEntry, newRoom, size, rooms);
                                generateEntry[door.getSide().opposite().getIndex()] = EntryType.DOOR;
                                rooms.set(newRoom, createRoom(randomLayout(), position, door, generateEntry));
                                linkAdjacentDoors(newRoom, size, rooms);
                                newLinks = true;
                            } else if(door.getSide().equals(Side.BOTTOM) && (newRoom = room+size) < size*size) {
                                position = new Vertex2((newRoom%size)*Interior.WIDTH, (newRoom/size)*Interior.HEIGHT);
                                randomEntry(generateEntry, newRoom, size, rooms);
                                generateEntry[door.getSide().opposite().getIndex()] = EntryType.DOOR;
                                rooms.set(newRoom, createRoom(randomLayout(), position, door, generateEntry));
                                linkAdjacentDoors(newRoom, size, rooms);
                                newLinks = true;
                            } else if(door.getSide().equals(Side.LEFT) && (newRoom = room-1) >= 0) {
                                position = new Vertex2((newRoom%size)*Interior.WIDTH, (newRoom/size)*Interior.HEIGHT);
                                randomEntry(generateEntry, newRoom, size, rooms);
                                generateEntry[door.getSide().opposite().getIndex()] = EntryType.DOOR;
                                rooms.set(newRoom, createRoom(randomLayout(), position, door, generateEntry));
                                linkAdjacentDoors(newRoom, size, rooms);
                                newLinks = true;
                            } else if(door.getSide().equals(Side.RIGHT) && (newRoom = room+1) < size*size) {
                                position = new Vertex2((newRoom%size)*Interior.WIDTH, (newRoom/size)*Interior.HEIGHT);
                                randomEntry(generateEntry, newRoom, size, rooms);
                                generateEntry[door.getSide().opposite().getIndex()] = EntryType.DOOR;
                                rooms.set(newRoom, createRoom(randomLayout(), position, door, generateEntry));
                                linkAdjacentDoors(newRoom, size, rooms);
                                newLinks = true;
                            }
                        }
                    }
                }
            }
        }
        
        // Find edge rooms and link them to generated exterior
        Exterior outer = createOuter(exteriorLayout, size, rooms);
        
        if(outer.getDoors().isEmpty()) {
            System.out.println("DONT USE THIS ONE");
            generateOuterDoors(outer, size, rooms);            
        }
        
        // Add attributes to level
        level.setExterior(outer);
        levelLayout.add(new SerializedRoom(null, 0));
        for(int i = 0; i < rooms.size(); i++) {
            if(rooms.get(i) != null) {
                levelLayout.add(new SerializedRoom(rooms.get(i).getLocation(), rooms.get(i).layout));
                level.addRoom(rooms.get(i));
            }
        }
        return level;
    }
    
    private static void generateOuterDoors(Exterior outer, int size, ArrayList<Interior> rooms) {
        int minTop = (size/2)+((size/2)*size);
        int maxBot = minTop;
        int minLeft = minTop;
        int maxRight = minTop;
        // find four edge rooms
        for(int i = 0; i < rooms.size(); i++) {
            if(rooms.get(i) != null) {
                int row = i/size;
                int col = i%size;
                if(row < minTop/size) {
                    minTop = i;
                }
                if(row > maxBot/size) {
                    maxBot = i;
                }
                if(col < minLeft%size) {
                    minLeft = i;
                }
                if(col > maxRight%size) {
                    maxRight = i;
                }
            }
        }
        // covert outer wall to door for TOP
        Interior room = rooms.get(minTop);
        for(Entity fgr: room.getForeground()) {
            if((int)fgr.getMinY() == room.getCenter().getY()-(Interior.HEIGHT/2)
                    && (int)fgr.getMinX() == room.getCenter().getX()-(EnvironmentTile.TILESIZE/2)) {
                Door door = forceAddDoorToRoom(room, (int)fgr.getMinX(), (int)fgr.getMinY(), Side.TOP);
                room.getForeground().remove(fgr);
                for(Entity fgo: outer.getForeground()) {
                    if((int)fgo.getMinY() == 112 && (int)fgo.getMinX() == 352) {
                        forceAddDoorToOuter(outer, (int) fgo.getMinX(), (int) fgo.getMinY(), door, Side.BOTTOM);
                        outer.getForeground().remove(fgo);
                        break;
                    }
                }
                break;
            }
        }
        // covert outer wall to door for TOP
        room = rooms.get(maxBot);
        for(Entity fgr: room.getForeground()) {
            if((int)fgr.getMinY() == room.getCenter().getY()+(Interior.HEIGHT/2)-EnvironmentTile.TILESIZE
                    && (int)fgr.getMinX() == room.getCenter().getX()-(EnvironmentTile.TILESIZE/2)) {
                Door door = forceAddDoorToRoom(room, (int)fgr.getMinX(), (int)fgr.getMinY(), Side.BOTTOM);
                room.getForeground().remove(fgr);
                for(Entity fgo: outer.getForeground()) {
                    if((int)fgo.getMinY() == 400 && (int)fgo.getMinX() == 352) {
                        forceAddDoorToOuter(outer, (int) fgo.getMinX(), (int) fgo.getMinY(), door, Side.TOP);
                        outer.getForeground().remove(fgo);
                        break;
                    }
                }
                break;
            }
        }
        // covert outer wall to door for TOP
        room = rooms.get(minLeft);
        for(Entity fgr: room.getForeground()) {
            if((int)fgr.getMinY() == room.getCenter().getY()-(EnvironmentTile.TILESIZE/2)
                    && (int)fgr.getMinX() == room.getCenter().getX()-(Interior.WIDTH/2)) {
                Door door = forceAddDoorToRoom(room, (int)fgr.getMinX(), (int)fgr.getMinY(), Side.LEFT);
                room.getForeground().remove(fgr);
                for(Entity fgo: outer.getForeground()) {
                    if((int)fgo.getMinY() == 256 && (int)fgo.getMinX() == 112) {
                        forceAddDoorToOuter(outer, (int) fgo.getMinX(), (int) fgo.getMinY(), door, Side.RIGHT);
                        outer.getForeground().remove(fgo);
                        break;
                    }
                }
                break;
            }
        }
        // covert outer wall to door for TOP
        room = rooms.get(maxRight);
        for(Entity fgr: room.getForeground()) {
            if((int)fgr.getMinY() == room.getCenter().getY()-(EnvironmentTile.TILESIZE/2)
                    && (int)fgr.getMinX() == room.getCenter().getX()+(Interior.WIDTH/2)-EnvironmentTile.TILESIZE) {
                Door door = forceAddDoorToRoom(room, (int)fgr.getMinX(), (int)fgr.getMinY(), Side.RIGHT);
                room.getForeground().remove(fgr);
                for(Entity fgo: outer.getForeground()) {
                    if((int)fgo.getMinY() == 256 && (int)fgo.getMinX() == 592) {
                        forceAddDoorToOuter(outer, (int) fgo.getMinX(), (int) fgo.getMinY(), door, Side.LEFT);
                        outer.getForeground().remove(fgo);
                        break;
                    }
                }
                break;
            }
        }
    }
    
    private static void forceAddDoorToOuter(Exterior outer, int x, int y, Door linkedDoor, Side side) {
        Vertex2 exitLoc;
        String doorPath;
        if(side.equals(Side.TOP)) {
            doorPath = tilesetPath+"tiles_mm1_elec/29.gif";
            exitLoc = new Vertex2(x, Door.TILESIZE + y);
        } else if(side.equals(Side.LEFT)) {
            doorPath = tilesetPath+"tiles_mm1_elec/38.gif";
            exitLoc = new Vertex2(Door.TILESIZE + x, y);
        } else if(side.equals(Side.RIGHT)) {
            doorPath = tilesetPath+"tiles_mm1_elec/38.gif";
            exitLoc = new Vertex2(x - Door.TILESIZE, y);
        } else {
            doorPath = tilesetPath+"tiles_mm1_elec/29.gif";
            exitLoc = new Vertex2(x, y - Door.TILESIZE);
        }
        outer.addDoor(new Door(game, doorPath, x, y, exitLoc, outer, linkedDoor, side));
    }
    
    private static Door forceAddDoorToRoom(Interior room, int x, int y, Side side) {
        Vertex2 exitLoc;
        String doorPath;
        
        if(side.equals(Side.TOP)) {
            doorPath = tilesetPath+"tiles_mm1_elec/29.gif";
            exitLoc = new Vertex2(x, Door.TILESIZE + y);
        } else if(side.equals(Side.LEFT)) {
            doorPath = tilesetPath+"tiles_mm1_elec/38.gif";
            exitLoc = new Vertex2(Door.TILESIZE + x, y);
        } else if(side.equals(Side.RIGHT)) {
            doorPath = tilesetPath+"tiles_mm1_elec/38.gif";
            exitLoc = new Vertex2(x - Door.TILESIZE, y);
        } else {
            doorPath = tilesetPath+"tiles_mm1_elec/29.gif";
            exitLoc = new Vertex2(x, y - Door.TILESIZE);
        }
        Door door = new Door(game, doorPath, x, y, exitLoc, room, null, side);
        room.addDoor(door);
        return door;
    }
    
    private static void linkAdjacentDoors(int index, int size, ArrayList<Interior> rooms) {
        Interior room = rooms.get(index);
        for(Door door: room.getDoors()) {
            if(door.getLink() == null) {
                int adjRoom = -1;
                if(door.getSide().equals(Side.TOP) && (adjRoom = index-size) >= 0) {
                    if(rooms.get(adjRoom) != null) {
                        for(Door adjDoor: rooms.get(adjRoom).getDoors()) {
                            if(adjDoor.getSide().opposite().equals(door.getSide())) {
                                door.setLink(adjDoor);
                            }
                        }
                    }
                } else if(door.getSide().equals(Side.BOTTOM) && (adjRoom = index+size) < size*size) {
                    if(rooms.get(adjRoom) != null) {
                        for(Door adjDoor: rooms.get(adjRoom).getDoors()) {
                            if(adjDoor.getSide().opposite().equals(door.getSide())) {
                                door.setLink(adjDoor);
                            }
                        }
                    }
                } else if(door.getSide().equals(Side.LEFT) && (adjRoom = index-1) >= 0) {
                    if(rooms.get(adjRoom) != null) {
                        for(Door adjDoor: rooms.get(adjRoom).getDoors()) {
                            if(adjDoor.getSide().opposite().equals(door.getSide())) {
                                door.setLink(adjDoor);
                            }
                        }
                    }
                } else if(door.getSide().equals(Side.RIGHT) && (adjRoom = index+1) < size*size) {
                    if(rooms.get(adjRoom) != null) {
                        for(Door adjDoor: rooms.get(adjRoom).getDoors()) {
                            if(adjDoor.getSide().opposite().equals(door.getSide())) {
                                door.setLink(adjDoor);
                            }
                        }
                    }
                }
            }
        }
    }
    
    private static void randomEntry(EntryType[] generateEntry, int index, int size, ArrayList<Interior> rooms) {
        for(int i = 0; i < generateEntry.length; i++) {
            double prob = Math.random();
            if(prob <= EntryType.NONE.getProbability()) {
                generateEntry[i] = EntryType.NONE;
            } else {
                prob -= EntryType.NONE.getProbability();
                if(prob <= EntryType.DOOR.getProbability()) {
                    generateEntry[i] = EntryType.DOOR;
                } else {
                    prob -= EntryType.DOOR.getProbability();
                    if(prob <= EntryType.PORTAL.getProbability()) {
                        generateEntry[i] = EntryType.PORTAL;
                    }
                }
            }
        }
        if(index-size >= 0 && rooms.get(index-size) != null && generateEntry[Side.TOP.getIndex()].equals(EntryType.DOOR)) {
            generateEntry[Side.TOP.getIndex()] = EntryType.NONE;
        }
        if(index+size < size*size && rooms.get(index+size) != null && generateEntry[Side.BOTTOM.getIndex()].equals(EntryType.DOOR)) {
            generateEntry[Side.BOTTOM.getIndex()] = EntryType.NONE;
        }
        if(index+1 < size*size && rooms.get(index+1) != null && generateEntry[Side.RIGHT.getIndex()].equals(EntryType.DOOR)) {
            generateEntry[Side.RIGHT.getIndex()] = EntryType.NONE;
        }
        if(index-1 >= 0 && rooms.get(index-1) != null && generateEntry[Side.LEFT.getIndex()].equals(EntryType.DOOR)) {
            generateEntry[Side.LEFT.getIndex()] = EntryType.NONE;
        }
    }
    
    private static int randomLayout() {
        return (int) (Math.random() * roomLayouts.length);
    }
    
    /**
     * loadLevel: Loads a room from a .oel (XML file)
     * 
     * @param filename
     */
    private static Interior createRoom(int layout, Vertex2 position, Door door, EntryType[] generateEntry) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(roomLayouts[layout])));
            Interior room = new Interior(position, layout);
            EnemyType enemyType = EnemyType.randomEnemy();
            String line = null;
            while((line = bufferedReader.readLine()) != null) {
                line = line.toLowerCase();
                
                //check foreground and tileset
                if(line.contains("foreground")) {
                    int x = position.getX(), y = position.getY();
                    String tileset = line.split("\"")[1];
                    while((line = bufferedReader.readLine()) != null && !line.toLowerCase().contains("foreground")) {
                        String[] parts = line.toLowerCase().split(",");
                        for(int p = 0; p < parts.length; p++) {
                            if(Integer.parseInt(parts[p]) != -1) {
                                room.addToForeground(new EnvironmentTile(game, tilesetPath+tileset+"/"+parts[p]+GIF, x, y/*new Vertex2(x, y)*/));
                            }
                            x += EnvironmentTile.TILESIZE;
                        }
                        x = position.getX();
                        y += EnvironmentTile.TILESIZE;
                    }
                }
                
                // ONLY CARE ABOUT BACKGROUND FOR CLIENT SIDE IGNORE ON SERVER SIDE
                /*if(line.contains("background")) {
                    int x = position.getX(), y = position.getY();
                    String tileset = line.split("\"")[1];
                    while((line = bufferedReader.readLine()) != null && !line.toLowerCase().contains("background")) {
                        String[] parts = line.toLowerCase().split(",");
                        for(int p = 0; p < parts.length; p++) {
                            if(Integer.parseInt(parts[p]) != -1) {
                                room.addToBackground(new EnvironmentTile(tilesetPath+tileset+"/"+parts[p]+GIF, new Vertex2(x, y)));
                            }
                            x += EnvironmentTile.TILESIZE;
                        }
                        x = position.getX();
                        y += EnvironmentTile.TILESIZE;
                    }
                }*/
                
                //objects
                if(line.contains("objects")) {
                    while((line = bufferedReader.readLine()) != null && !line.toLowerCase().contains("objects")) {
                        String[] parts = line.toLowerCase().split("\\s+");
                        int x = Integer.parseInt(parts[3].split("\"")[1]) + position.getX();
                        int y = Integer.parseInt(parts[4].split("\"")[1]) + position.getY();
                        // NOTE WHEN SPAWNING NEW ENEMIES CHANGE CONSTRUCTOR TO TAKE IN ENUM FACE.RIGHT/LEFT/UP/DOWN
                        if(parts[1].contains("playerspawn")) {
                            //room.addPlayer(new Player(game, "spawn1.gif", x, y, x+11, y+19, 12, 11, 3, 0));
                            room.setPortalExit(new Vertex2(x, y));
                        }
                        else if(parts[1].contains("enemyspawn")) {
                            if(enemyType.equals(EnemyType.SHIELD)) {
                                room.addEnemy(new ShieldGuyEntity(game, enemyType.getPath()+"ShieldGuy1.gif", x, y, x+1, y+2, 24, 22, room));
                            } else if(enemyType.equals(EnemyType.WOOD)) {
                                room.addEnemy(new WoodManEntity(game, enemyType.getPath()+"woodman1.gif", x, y, x+6, y+6, 30, 26, room));
                            } else if(enemyType.equals(EnemyType.CANNON)) {
                                room.addEnemy(new CannonEntity(game, enemyType.getPath()+"cannon1floor.gif", x, y, x, y, 35, 25, room));
                            }
                        }
                        else if(parts[1].contains("spike")) {
                            room.addTrap(new SpikeEntity(game, "spikeFloor.gif", x, y, x+7, y+2, 10, 15));
                        }
                        else if(parts[1].contains("door")) {
                            Side side = Side.findByValue(parts[5].split("\"")[1]);
                            if(generateEntry[side.getIndex()].equals(EntryType.DOOR)) {
                                Vertex2 exitLoc;
                                String doorPath;
                                
                                if(side.equals(Side.TOP)) {
                                    doorPath = tilesetPath+"tiles_mm1_elec/29.gif";
                                    exitLoc = new Vertex2(x, Door.TILESIZE + y);
                                } else if(side.equals(Side.LEFT)) {
                                    doorPath = tilesetPath+"tiles_mm1_elec/38.gif";
                                    exitLoc = new Vertex2(Door.TILESIZE + x, y);
                                } else if(side.equals(Side.RIGHT)) {
                                    doorPath = tilesetPath+"tiles_mm1_elec/38.gif";
                                    exitLoc = new Vertex2(x - Door.TILESIZE, y);
                                } else {
                                    doorPath = tilesetPath+"tiles_mm1_elec/29.gif";
                                    exitLoc = new Vertex2(x, y - Door.TILESIZE);
                                }
                                
                                if(door != null && door.getSide().opposite().equals(side)) {
                                    room.addDoor(new Door(game, doorPath, x, y, exitLoc, room, door, side));
                                } else {
                                    room.addDoor(new Door(game, doorPath, x, y, exitLoc, room, null, side));
                                }
                            } else if (generateEntry[side.getIndex()].equals(EntryType.PORTAL)) {
                                Vertex2 gkLoc;
                                if(side.equals(Side.TOP)) {
                                    gkLoc = new Vertex2(Door.TILESIZE + x, Door.TILESIZE + y);
                                } else if(side.equals(Side.LEFT)) {
                                    gkLoc = new Vertex2(Door.TILESIZE + x, Door.TILESIZE + y);
                                } else if(side.equals(Side.RIGHT)) {
                                    gkLoc = new Vertex2(x - Door.TILESIZE, y - Door.TILESIZE);
                                } else {
                                    gkLoc = new Vertex2(x - Door.TILESIZE, y - Door.TILESIZE);
                                }
                                
                                Portal portal = new Portal(game, tilesetPath+"tiles_mm1_elec/6.gif", x, y, room, level.getRooms());
                                room.addPortals(portal);
                                room.addGateKeeper(new GateKeeper(game, animationPath+"alien/alien.gif", gkLoc.getX(), gkLoc.getY(), gkLoc.getX(), gkLoc.getY(),43,29, portal));
                            } else {
                                room.addToForeground(new EnvironmentTile(game, tilesetPath+"invisible.gif", x, y));
                            }
                        }
                        // ADD OTHER OBJECTS HERE
                    }
                }
            }
            return room;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } 
    }
    
    private static Exterior createOuter(String layout, int size, ArrayList<Interior> rooms) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(layout)));
            Exterior outer = new Exterior(0);
            String line = null;
            while((line = bufferedReader.readLine()) != null) {
                line = line.toLowerCase();
                
                //check foreground and tileset
                if(line.contains("foreground")) {
                    int x = 0, y = 0;
                    String tileset = line.split("\"")[1];
                    while((line = bufferedReader.readLine()) != null && !line.toLowerCase().contains("foreground")) {
                        String[] parts = line.toLowerCase().split(",");
                        for(int p = 0; p < parts.length; p++) {
                            if(Integer.parseInt(parts[p]) != -1) {
                                outer.addToForeground(new EnvironmentTile(game, tilesetPath+tileset+"/"+parts[p]+GIF, x, y/*new Vertex2(x, y)*/));
                            }
                            x += EnvironmentTile.TILESIZE;
                        }
                        x = 0;
                        y += EnvironmentTile.TILESIZE;
                    }
                }
                
                // ONLY CARE ABOUT BACKGROUND FOR CLIENT SIDE IGNORE ON SERVER SIDE
                /*if(line.contains("background")) {
                    int x = position.getX(), y = position.getY();
                    String tileset = line.split("\"")[1];
                    while((line = bufferedReader.readLine()) != null && !line.toLowerCase().contains("background")) {
                        String[] parts = line.toLowerCase().split(",");
                        for(int p = 0; p < parts.length; p++) {
                            if(Integer.parseInt(parts[p]) != -1) {
                                room.addToBackground(new EnvironmentTile(tilesetPath+tileset+"/"+parts[p]+GIF, new Vertex2(x, y)));
                            }
                            x += EnvironmentTile.TILESIZE;
                        }
                        x = position.getX();
                        y += EnvironmentTile.TILESIZE;
                    }
                }*/
                
                //objects
                if(line.contains("objects")) {
                    while((line = bufferedReader.readLine()) != null && !line.toLowerCase().contains("objects")) {
                        String[] parts = line.toLowerCase().split("\\s+");
                        int x = Integer.parseInt(parts[3].split("\"")[1]);
                        int y = Integer.parseInt(parts[4].split("\"")[1]);
                        // NOTE WHEN SPAWNING NEW ENEMIES CHANGE CONSTRUCTOR TO TAKE IN ENUM FACE.RIGHT/LEFT/UP/DOWN
                        if(parts[1].contains("playerspawn")) {
                            outer.addPlayerSpawn(new Vertex2(x, y));
                        }
                        else if(parts[1].contains("door")) {
                            Side side = Side.findByValue(parts[5].split("\"")[1]);
                            int row = Integer.parseInt(parts[6].split("\"")[1]);
                            int col = Integer.parseInt(parts[7].split("\"")[1]); 
                            Vertex2 exitLoc;
                            String doorPath;
                            if(side.equals(Side.TOP)) {
                                doorPath = tilesetPath+"tiles_mm1_elec/29.gif";
                                exitLoc = new Vertex2(x, Door.TILESIZE + y);
                            } else if(side.equals(Side.LEFT)) {
                                doorPath = tilesetPath+"tiles_mm1_elec/38.gif";
                                exitLoc = new Vertex2(Door.TILESIZE + x, y);
                            } else if(side.equals(Side.RIGHT)) {
                                doorPath = tilesetPath+"tiles_mm1_elec/38.gif";
                                exitLoc = new Vertex2(x - Door.TILESIZE, y);
                            } else {
                                doorPath = tilesetPath+"tiles_mm1_elec/29.gif";
                                exitLoc = new Vertex2(x, y - Door.TILESIZE);
                            }
                            
                            boolean doorLinked = false;
                            if(rooms.get(col+(row*size)) != null) {
                                for(Door door: rooms.get(col+(row*size)).getDoors()) {
                                    if(door.getSide().opposite().equals(side)) {
                                        outer.addDoor(new Door(game, doorPath, x, y, exitLoc, outer, door, side));
                                        doorLinked = true;
                                        break;
                                    }
                                }
                            }
                            if(!doorLinked) {
                                outer.addToForeground(new EnvironmentTile(game, tilesetPath+"invisible.gif", x, y));
                            }
                        }
                        // ADD OTHER OBJECTS HERE
                    }
                }
            }
            return outer;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } 
    }
}
