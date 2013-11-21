package game;

import java.awt.Rectangle;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.java.games.input.*;

import org.lwjgl.Sys;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import engine.inputhandler.Axis;
import engine.inputhandler.Button;
import engine.inputhandler.Input;
import engine.inputhandler.PhysicalInput;
import engine.render.IDisplay;
import engine.render.Sprite;
import engine.serializable.SerializedEntity;
import engine.serializable.SerializedObject;
import engine.serializable.SerializedRoom;
import engine.soundmanager.SoundManager;

/*
* Classname:            GameEngine.java
*
* Version information:  1.0
*
* Date:                 10/30/2013
*
* Copyright notice:     Copyright (c) 2013 Garrett Benoit
*/

/**
 * GameEngine: Implementation of a game loop. Runs a game implementing the
 * IGame interface.
 */
public class GameEngine {
    /** the number of frames per second that we want to run the game at */
    private final int FPS = 60;
    
    /** flag indicating that we're playing the game */
    private boolean playingGame = true;
    
    /** the game to run */
    private Game theGame;
    
    /** a map of physical inputs to names */
    private static HashMap<PhysicalInput, String> inputMap;
    
    /** a list of inputs to listen for */
    private ArrayList<Input> inputs;
    
    /** the number of timer ticks per second */
    private static long timerTicksPerSecond = Sys.getTimerResolution();
    
    /**
     * The time at which the last rendering looped started from the point
     * of view of the game logic
     */
    private long lastLoopTime = getTime();
    
    /** SoundManager to make sound with */
    private static SoundManager soundManager;
    
    /** the display object to use */
    private IDisplay theDisplay = null;

    /** The time since the last record of fps */
    private long                    lastFpsTime;

    /** The recorded fps */
    private int                     fps;
    
    protected final static int READ_TIMEOUT = 1000;
    protected final static int SERVER_PORT = 10500;
    protected final static String IP_ADDRESS = "localhost";

    protected static Socket socket;
    protected static ObjectInputStream ois;
    protected static ObjectOutputStream oos;
    
    public enum Pressed {
        RIGHT(0), LEFT(1), UP(2), DOWN(3), FIRE(4), ESCAPE(5), PAUSE(6), START_GAME(7), 
        SELECT_FORWARD(8),SELECT_BACKWARD(9); 
        private final int value;
        private Pressed(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
    };
    
    /**
     * Constructor: Constructor for the game engine. Sets up the inputs.
     */
    public GameEngine() {
        initInput();
        initSound();
    }
    
    private void initInput() {
     // initialize input map
        inputMap = new HashMap<PhysicalInput, String>();
        
        // populate input map
        inputMap.put(PhysicalInput.KEYBOARD_BACK, "Keyboard:Back");
        inputMap.put(PhysicalInput.KEYBOARD_TAB, "Keyboard:Tab");
        inputMap.put(PhysicalInput.KEYBOARD_RETURN, "Keyboard:Return");
        inputMap.put(PhysicalInput.KEYBOARD_LEFT_SHIFT,
                "Keyboard:Left Shift");
        inputMap.put(PhysicalInput.KEYBOARD_LEFT_CONTROL,
                "Keyboard:Left Control");
        inputMap.put(PhysicalInput.KEYBOARD_LEFT_ALT, "Keyboard:Left Alt");
        inputMap.put(PhysicalInput.KEYBOARD_PAUSE, "Keyboard:Pause");
        inputMap.put(PhysicalInput.KEYBOARD_CAPS_LOCK,
                "Keyboard:Caps Lock");
        inputMap.put(PhysicalInput.KEYBOARD_ESCAPE, "Keyboard:Escape");
        inputMap.put(PhysicalInput.KEYBOARD_PG_UP, "Keyboard:Pg Up");
        inputMap.put(PhysicalInput.KEYBOARD_PG_DOWN, "Keyboard:Pg Down");
        inputMap.put(PhysicalInput.KEYBOARD_END, "Keyboard:End");
        inputMap.put(PhysicalInput.KEYBOARD_HOME, "Keyboard:Home");
        inputMap.put(PhysicalInput.KEYBOARD_LEFT, "Keyboard:Left");
        inputMap.put(PhysicalInput.KEYBOARD_UP, "Keyboard:Up");
        inputMap.put(PhysicalInput.KEYBOARD_RIGHT, "Keyboard:Right");
        inputMap.put(PhysicalInput.KEYBOARD_DOWN, "Keyboard:Down");
        inputMap.put(PhysicalInput.KEYBOARD_SYSRQ, "Keyboard:SysRq");
        inputMap.put(PhysicalInput.KEYBOARD_INSERT, "Keyboard:Insert");
        inputMap.put(PhysicalInput.KEYBOARD_DELETE, "Keyboard:Delete");
        inputMap.put(PhysicalInput.KEYBOARD_0, "Keyboard:0");
        inputMap.put(PhysicalInput.KEYBOARD_1, "Keyboard:1");
        inputMap.put(PhysicalInput.KEYBOARD_2, "Keyboard:2");
        inputMap.put(PhysicalInput.KEYBOARD_3, "Keyboard:3");
        inputMap.put(PhysicalInput.KEYBOARD_4, "Keyboard:4");
        inputMap.put(PhysicalInput.KEYBOARD_5, "Keyboard:5");
        inputMap.put(PhysicalInput.KEYBOARD_6, "Keyboard:6");
        inputMap.put(PhysicalInput.KEYBOARD_7, "Keyboard:7");
        inputMap.put(PhysicalInput.KEYBOARD_8, "Keyboard:8");
        inputMap.put(PhysicalInput.KEYBOARD_9, "Keyboard:9");
        inputMap.put(PhysicalInput.KEYBOARD_A, "Keyboard:A");
        inputMap.put(PhysicalInput.KEYBOARD_B, "Keyboard:B");
        inputMap.put(PhysicalInput.KEYBOARD_C, "Keyboard:C");
        inputMap.put(PhysicalInput.KEYBOARD_D, "Keyboard:D");
        inputMap.put(PhysicalInput.KEYBOARD_E, "Keyboard:E");
        inputMap.put(PhysicalInput.KEYBOARD_F, "Keyboard:F");
        inputMap.put(PhysicalInput.KEYBOARD_G, "Keyboard:G");
        inputMap.put(PhysicalInput.KEYBOARD_H, "Keyboard:H");
        inputMap.put(PhysicalInput.KEYBOARD_I, "Keyboard:I");
        inputMap.put(PhysicalInput.KEYBOARD_J, "Keyboard:J");
        inputMap.put(PhysicalInput.KEYBOARD_K, "Keyboard:K");
        inputMap.put(PhysicalInput.KEYBOARD_L, "Keyboard:L");
        inputMap.put(PhysicalInput.KEYBOARD_M, "Keyboard:M");
        inputMap.put(PhysicalInput.KEYBOARD_N, "Keyboard:N");
        inputMap.put(PhysicalInput.KEYBOARD_O, "Keyboard:O");
        inputMap.put(PhysicalInput.KEYBOARD_P, "Keyboard:P");
        inputMap.put(PhysicalInput.KEYBOARD_Q, "Keyboard:Q");
        inputMap.put(PhysicalInput.KEYBOARD_R, "Keyboard:R");
        inputMap.put(PhysicalInput.KEYBOARD_S, "Keyboard:S");
        inputMap.put(PhysicalInput.KEYBOARD_T, "Keyboard:T");
        inputMap.put(PhysicalInput.KEYBOARD_U, "Keyboard:U");
        inputMap.put(PhysicalInput.KEYBOARD_V, "Keyboard:V");
        inputMap.put(PhysicalInput.KEYBOARD_W, "Keyboard:W");
        inputMap.put(PhysicalInput.KEYBOARD_X, "Keyboard:X");
        inputMap.put(PhysicalInput.KEYBOARD_Y, "Keyboard:Y");
        inputMap.put(PhysicalInput.KEYBOARD_Z, "Keyboard:Z");
        inputMap.put(PhysicalInput.KEYBOARD_LEFT_WINDOWS,
                "Keyboard:Left Windows");
        inputMap.put(PhysicalInput.KEYBOARD_RIGHT_WINDOWS,
                "Keyboard:Right Windows");
        inputMap.put(PhysicalInput.KEYBOARD_NUM_0, "Keyboard:Num 0");
        inputMap.put(PhysicalInput.KEYBOARD_NUM_1, "Keyboard:Num 1");
        inputMap.put(PhysicalInput.KEYBOARD_NUM_2, "Keyboard:Num 2");
        inputMap.put(PhysicalInput.KEYBOARD_NUM_3, "Keyboard:Num 3");
        inputMap.put(PhysicalInput.KEYBOARD_NUM_4, "Keyboard:Num 4");
        inputMap.put(PhysicalInput.KEYBOARD_NUM_5, "Keyboard:Num 5");
        inputMap.put(PhysicalInput.KEYBOARD_NUM_6, "Keyboard:Num 6");
        inputMap.put(PhysicalInput.KEYBOARD_NUM_7, "Keyboard:Num 7");
        inputMap.put(PhysicalInput.KEYBOARD_NUM_8, "Keyboard:Num 8");
        inputMap.put(PhysicalInput.KEYBOARD_NUM_9, "Keyboard:Num 9");
        inputMap.put(PhysicalInput.KEYBOARD_MULTIPLY, "Keyboard:Multiply");
        inputMap.put(PhysicalInput.KEYBOARD_NUM_PLUS, "Keyboard:Num +");
        inputMap.put(PhysicalInput.KEYBOARD_NUM_MINUS, "Keyboard:Num -");
        inputMap.put(PhysicalInput.KEYBOARD_NUM_DOT, "Keyboard:Num .");
        inputMap.put(PhysicalInput.KEYBOARD_NUM_DIVIDE, "Keyboard:Num /");
        inputMap.put(PhysicalInput.KEYBOARD_F1, "Keyboard:F1");
        inputMap.put(PhysicalInput.KEYBOARD_F2, "Keyboard:F2");
        inputMap.put(PhysicalInput.KEYBOARD_F3, "Keyboard:F3");
        inputMap.put(PhysicalInput.KEYBOARD_F4, "Keyboard:F4");
        inputMap.put(PhysicalInput.KEYBOARD_F5, "Keyboard:F5");
        inputMap.put(PhysicalInput.KEYBOARD_F6, "Keyboard:F6");
        inputMap.put(PhysicalInput.KEYBOARD_F7, "Keyboard:F7");
        inputMap.put(PhysicalInput.KEYBOARD_F8, "Keyboard:F8");
        inputMap.put(PhysicalInput.KEYBOARD_F9, "Keyboard:F9");
        inputMap.put(PhysicalInput.KEYBOARD_F10, "Keyboard:F10");
        inputMap.put(PhysicalInput.KEYBOARD_F11, "Keyboard:F11");
        inputMap.put(PhysicalInput.KEYBOARD_F12, "Keyboard:F12");
        inputMap.put(PhysicalInput.KEYBOARD_F13, "Keyboard:F13");
        inputMap.put(PhysicalInput.KEYBOARD_F14, "Keyboard:F14");
        inputMap.put(PhysicalInput.KEYBOARD_F15, "Keyboard:F15");
        inputMap.put(PhysicalInput.KEYBOARD_NUM_LOCK, "Keyboard:Num Lock");
        inputMap.put(PhysicalInput.KEYBOARD_SCROLL_LOCK,
                "Keyboard:Scroll Lock");
        inputMap.put(PhysicalInput.KEYBOARD_COMMA, "Keyboard:,");
        inputMap.put(PhysicalInput.KEYBOARD_DOT, "Keyboard:.");
        inputMap.put(PhysicalInput.KEYBOARD_TILDE, "Keyboard:~");
        inputMap.put(PhysicalInput.KEYBOARD_LEFT_BRACKET, "Keyboard:[");
        inputMap.put(PhysicalInput.KEYBOARD_RIGHT_BRACKET, "Keyboard:]");
        inputMap.put(PhysicalInput.MOUSE_X, "Mouse:x");
        inputMap.put(PhysicalInput.MOUSE_Y, "Mouse:y");
        inputMap.put(PhysicalInput.MOUSE_Z, "Mouse:z");
        inputMap.put(PhysicalInput.MOUSE_LEFT, "Mouse:Left");
        inputMap.put(PhysicalInput.MOUSE_RIGHT, "Mouse:Right");
        inputMap.put(PhysicalInput.MOUSE_MIDDLE, "Mouse:Middle");
    }
    
    private void initSound() {
        soundManager = new SoundManager();
        soundManager.initialize(8);
    }
    
    /**
     * setDisplay: Sets the display
     * 
     * @param aDisplay
     *            the display to use, if not null
     * @throws Exception
     *             if the display is null
     */
    private void setDisplay(IDisplay aDisplay) throws Exception {
        if (aDisplay == null) {
            throw new Exception("Null display");
        }
        theDisplay = aDisplay;
    }
    
    /**
     * run: runs the game loop on a game implementing the IGame interface.
     * 
     * @param aGame
     *            the game to run.
     * @throws Exception
     */
    public void run(Game aGame) throws Exception {
        theGame = aGame;
        setDisplay(theGame.getDisplay());
        theDisplay.init();
        createSocket();
        inputs = theGame.initInputs(checkForServerLevel());
        
        gameLoop();
        theGame.shutdown();
    }
    
    /**
     * gameLoop: a timer-based game loop to run the game
     * @throws IOException 
     * @throws ClassNotFoundException 
     */
    private void gameLoop() throws IOException, ClassNotFoundException {        
        // Game loop runs while the player is playing
        lastLoopTime = getTime();
        while (playingGame) {
            // clear screen
            theDisplay.reset();
            
            // Get the input
            getInputs();
            List<Pressed> inputs = new ArrayList<Pressed>();
            if(((MazeGameClient) theGame).right.isDown()) inputs.add(Pressed.RIGHT);
            if(((MazeGameClient) theGame).left.isDown()) inputs.add(Pressed.LEFT);
            if(((MazeGameClient) theGame).up.isDown()) inputs.add(Pressed.UP);
            if(((MazeGameClient) theGame).down.isDown()) inputs.add(Pressed.DOWN);
            if(((MazeGameClient) theGame).fire.isDown()) inputs.add(Pressed.FIRE);
            if(((MazeGameClient) theGame).escape.isDown()) inputs.add(Pressed.ESCAPE);
            if(((MazeGameClient) theGame).pause.isDown()) inputs.add(Pressed.PAUSE);
            //if(((MazeGameClient) theGame).cameraMode.isDown()) inputs.add("cameraMode");
            if(((MazeGameClient) theGame).startGame.isDown()) inputs.add(Pressed.START_GAME);
            
            
            long delta = getTime() - lastLoopTime;
            lastLoopTime = getTime();
            lastFpsTime += delta;
            fps++;
            
            if (lastFpsTime > 1000) {
                Display.setTitle("(FPS: " + fps + ")");
                lastFpsTime = 0;
                fps = 0;
            }
            
            sendInputsToServer(inputs);
            
            // Update the world
            List<SerializedObject> updatedObjects = checkForServerUpdates();
            theGame.update(delta, updatedObjects);
            
            // Paint the graphics
            render();
            if(updatedObjects != null) {
                for(SerializedObject so: updatedObjects) {
                    if(so instanceof SerializedEntity) {
                        SerializedEntity se = (SerializedEntity)so;
                        //System.out.println(so.getID() + "\t" + so.getImage() + "\t" + so.getPosition() + "\t" + so.needsDelete());
                        Sprite sprite = theGame.getDisplay().getSprite(se.getImage());
                        sprite.draw((int)se.getPosition().getX(), (int)se.getPosition().getY());
                    }
                }
            }
            
            // update window contents
            theDisplay.update();
            if (theGame.isDone()) {
                playingGame = false;
            }
        }
        
        // clean up
        soundManager.destroy();
        
        // Close Game window
        theDisplay.quit();
    }
    
    /**
     * Creates the socket
     */
    protected static void createSocket()throws
            UnknownHostException, IOException {

        socket = new Socket(IP_ADDRESS, SERVER_PORT);
        oos = new ObjectOutputStream(socket.getOutputStream());
        oos.flush();
        ois = new ObjectInputStream(socket.getInputStream());
        socket.setSoTimeout(READ_TIMEOUT);
    }
    
    /**
     * send inputs to server
     */
    protected static void sendInputsToServer(List<Pressed> inputs2) throws IOException {
        if(inputs2 != null) oos.writeObject(inputs2);
    }
 
    /**
     * Wait for server to provide list of updates
     */
    @SuppressWarnings("unchecked")
    protected static List<SerializedObject> checkForServerUpdates() throws IOException, ClassNotFoundException {
        try {
            return (List<SerializedObject>) ois.readObject();
        } catch(Exception ignore) {}
        return null;
    }
    
    @SuppressWarnings("unchecked")
    protected static List<SerializedRoom> checkForServerLevel() {
        try {
            return (List<SerializedRoom>) ois.readObject();
        } catch(Exception ignore) {}
        return null;
    }
    
    /**
     * getInput: Get a list of the input components to track
     */
    private void getInputs() {
        for (Input i : inputs) {
            boolean hasDown = false;
            float pollValue = 0.0f;
            String deviceType = "";
            for (PhysicalInput p : i.getPhysicalInputs()) {
                String physInputName = inputMap.get(p);
                String inputName = physInputName.substring(physInputName.indexOf(':') + 1);
                String inputType = physInputName.substring(0, physInputName.indexOf(':'));
                ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment();
                // Check each controller for input
                for (Controller controller : ce.getControllers()) {
                    deviceType = controller.getType().toString();
                    if (controller != null && hasDown == false && deviceType.equalsIgnoreCase(inputType)) {
                        for(Component com: controller.getComponents()) {
                            if (com.getName().equalsIgnoreCase(inputName)) {
                                controller.poll();
                                pollValue = com.getPollData();
                                if (i instanceof Button && pollValue == 1.0f) {
                                        hasDown = true;
                                }
                                break;
                            }
                        }
                    }
                    if(hasDown == true || pollValue > 0) { // Added pollValue > 0 --- might be wrong implementation for joystick
                        break;
                    }
                }
                
            }
            if (i instanceof Button) {
                if (hasDown) {
                    ((Button) i).setDown(true);
                } else {
                    ((Button) i).setDown(false);
                }
            }else if (i instanceof Axis) {
                ((Axis) i).setValue(pollValue);
            }
        }
    }
    
    /**
     * Get the high resolution time in milliseconds
     * 
     * @return The high resolution time in milliseconds
     */
    public static long getTime() {
        // we get the "timer ticks" from the high resolution timer
        // multiply by 1000 so our end result is in milliseconds
        // then divide by the number of ticks in a second giving
        // us a nice clear time in milliseconds
        return (Sys.getTime() * 1000) / timerTicksPerSecond;
    }
    
    /**
     * render: Syncs the display to FPS
     */
    public void render() {
        theDisplay.sync(FPS);
        drawEntities();
    }
    
    /**
     * drawEntities: calls draw on all the entities in the Game
     */
    public void drawEntities() {
        for (Entity ent : theGame.getEntities()) {
            if(ent != null) {
                ent.draw();
            }
        }
    }

    public static int addSound(String fileName) {
        return soundManager.addSound(fileName);
    }
    
    public static void playSound(int soundNum) {
        soundManager.playEffect(soundNum);
    }
    
    public static void playMusic(int soundNum) {
        soundManager.playSound(soundNum);
    }
    
    public static void setMouseHidden(boolean mouseHidden) {
        Mouse.setGrabbed(mouseHidden);
    }
}
