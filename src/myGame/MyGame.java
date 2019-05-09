package myGame;

import com.bulletphysics.linearmath.QuaternionUtil;
import myGameEngine.*;
import networking.*;

import java.awt.*;
import java.io.*;

import ray.rage.*;
import ray.rage.asset.material.Material;
import ray.rage.game.*;
import ray.rage.rendersystem.*;
import ray.rage.rendersystem.Renderable.*;
import ray.rage.scene.*;
import ray.rage.scene.Camera.Frustum.*;
import ray.rage.scene.controllers.*;
import ray.rml.*;
import ray.rage.rendersystem.gl4.GL4RenderSystem;
import ray.rage.rendersystem.states.*;
import ray.rage.asset.texture.*;
import ray.input.*;
import ray.input.action.*;
import ray.rage.rendersystem.shader.*;

import static ray.rage.scene.SkeletalEntity.EndType.*;

import ray.rage.util.BufferUtil;


import java.nio.FloatBuffer;
import java.nio.IntBuffer;

// skybox imports

import ray.rage.util.*;
import ray.rage.rendersystem.states.*;
import ray.rage.asset.texture.*;
import ray.rage.util.*;

import java.awt.geom.*;

// javascript imports
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.util.*;
import java.util.List;

//input Action
import net.java.games.input.Event;

//networking
import java.lang.Exception;
import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import ray.networking.IGameConnection.ProtocolType;

import java.net.UnknownHostException;

// physics
import ray.physics.PhysicsEngine;
import ray.physics.PhysicsObject;
import ray.physics.PhysicsEngineFactory;


public class MyGame extends VariableFrameRateGame {
    // to minimize variable allocation in update()
    private GL4RenderSystem rs;
    private float elapsTime = 0.0f;
    private String elapsTimeStr, player1HUD, player2HUD;
    private int elapsTimeSec;

    private Camera camera1;
    private Camera camera2;

    private SceneNode player1Node;
    private SceneNode player2Node;

    private InputManager im;
    private Action quitGameAction, toggleJSAction;

    private int player1PlanetsVisited, player1Score = 0;
    private int player2PlanetsVisited, player2Score = 0;

    private int scoreIncrement = 100;
    private float movementSpeed = 0.08f, rotationAmount = 1.0f;

    private Camera3Pcontroller orbitController1, orbitController2;
    private PlayerController playerController1, playerController2, physController1, physController2;


    private static final String SKYBOX_NAME = "SkyBox";
    private boolean allowJavascripts = false; // javascripts can be enabled/disabled

    //Networking

    private SceneNode specialItemN;
    private String serverAddress;
    private int serverPort;
    private ProtocolType serverProtocol;
    private static ProtocolClient protClient;
    private boolean isClientConnected;
    private LinkedList<UUID> gameObjectsToRemove;
    private static MyGame game;
    private GameServerUDP thisUDPServer;
    private NPCcontroller npcCtrl;
    ManualObject specialItem;

    private static String networkType; //going to need to be nonestatic at some point


    // physics
    private SceneNode ball1Node, ball2Node, gndNode;// TESTING
    private SceneNode cameraPositionNode;// TESTING
    private final static String GROUND_E = "Ground";// TESTING
    private final static String GROUND_N = "GroundNode"; // TESTING
    private PhysicsEngine physicsEng;
    private PhysicsObject ball1PhysObj, ball2PhysObj, gndPlaneP; // TESTING

    private SceneNode cubeTestNode, roundNode; // my test
    private PhysicsObject cubeTestPhysObj, roundNodePhysObj;

    private PhysicsObject playerPhysObj;


    private static String playerModel;
    private static String playerTexture;
    public static String playerSkeleton;
    private boolean playerOneWins = false, NPCWins = false, ghostWon = false, gameLoaded = false, gameOver = false;
    private String winGameTime, currentTimeStr = "-";

    private SceneNode wholeMazeNode;

    private Engine myEngine;
    private LinkedList<GhostAvatar> ghostAvatars = new LinkedList<GhostAvatar>();
    private LinkedList<Boolean> ghostAvatarWin = new LinkedList<Boolean>();

    private Light spotlight;


    public MyGame(String serverAddr, int sPort) {
        super();
        this.serverAddress = serverAddr;
        this.serverPort = sPort;
        this.serverProtocol = ProtocolType.UDP;
        if (networkType.compareTo("c") == 0 || networkType.compareTo("m") == 0) {
            System.out.println("WASD keys to move");
            System.out.println("Q and E keys to turn");
            System.out.println("arrow keys to rotate the camera");
            System.out.println("Z and X keys to zoom the camera");
            System.out.println("Press SPACEBAR to toggle javascripts");
            System.out.println("press J for dance animation(only on black robot)");
            System.out.println("press B to toggle flashlight");
            System.out.println("When connected to server, press n from any client to start NPCs");
        }


        /*
        System.out.println("PLAYER 2: (PS4 controller)");
        System.out.println("press the directional buttons to move");
        System.out.println("L1 and R1 to turn");
        System.out.println("use L2, R2, Triangle and X buttons to rotate the camera");
        System.out.println("Square and Circle buttons to zoom the camera");
        System.out.println("press ESC to quit game");
        */
        myEngine = getEngine();
    }

    public static void main(String[] args) {
        //ask about which player
        String playerChoice = "";
        networkType = args[2]; // s for server, c for client
        if (networkType.compareTo("c") == 0 || networkType.compareTo("m") == 0) {
            Scanner modelScanner = new Scanner(System.in);  // Create a Scanner object
            System.out.println("Choose your character:");
            System.out.println("press d for the The black robot");
            System.out.println("press c for the purple robot");
            playerChoice = modelScanner.nextLine();
        }

        if (playerChoice.equals("c")) {
            playerModel = "robo2.rkm";
            playerTexture = "robot.png";
            playerSkeleton = "robo2.rks";
        } else if (playerChoice.equals("d")) {
            playerModel = "robo.rkm";
            playerTexture = "cTxt.png";
            playerSkeleton = "robo.rks";
        }

        game = new MyGame(args[0], Integer.parseInt(args[1]));

        try {
            game.startup();
            game.run();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            protClient.sendByeMessage();
            //game.shutdown();
            game.exit();
        }
    }

    @Override
    protected void setupWindow(RenderSystem rs, GraphicsEnvironment ge) {
        if (networkType.equals("s")) {
            rs.createRenderWindow(new DisplayMode(1000, 700, 24, 60), false);
        } else {
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            DisplaySettingsDialog dsd = new DisplaySettingsDialog(ge.getDefaultScreenDevice());
            dsd.showIt();
            RenderWindow rw = rs.createRenderWindow(dsd.getSelectedDisplayMode(), dsd.isFullScreenModeSelected());
        }


    }
//    private void tryFullScreenMode(GraphicsDevice gd, DisplayMode dispMode)
//    { if (gd.isFullScreenSupported())
//
//    { gd.setUndecorated(true);
//        frame.setResizable(false);
//        // AWT repaint events unecessary – we manage render loop
//        frame.setIgnoreRepaint(true);
//        gd.setFullScreenWindow(frame);
//        if (gd.isDisplayChangeSupported())
//        { try
//        { gd.setDisplayMode(dispMode);
//            frame.setSize(dispMode.getWidth(), dispMode.getHeight());
//            isInFullScreenMode = true;
//        } catch (IllegalArgumentException e)
//        { frame.setUndecorated(false);
//            frame.setResizable(true);
//        }} else {
//            logger.fine("FSEM not supported");
//        }} else {
//        frame.setUndecorated(false);
//        frame.setResizable(true);
//        frame.setSize(dispMode.getWidth(), dispMode.getHeight());
//        frame.setLocationRelativeTo(null);
//    }
//    }

	/*
    //  now we add setting up viewports in the window
    protected void setupWindowViewports(RenderWindow rw)
    {
        rw.addKeyListener(this);
        Viewport topViewport = rw.getViewport(0);
        topViewport.setDimensions(.04f, .01f, .99f, .49f);// B,L,W,H
        topViewport.setClearColor(new Color(.04f, .3f, .5f));
        //Viewport botViewport = rw.createViewport(.01f, .01f, .99f, .49f);
        //botViewport.setClearColor(new Color(.04f, .3f, .5f));
    }
    */

    //  we need a camera for each viewport
    @Override
    protected void setupCameras(SceneManager sm, RenderWindow rw) {
        SceneNode rootNode = sm.getRootSceneNode();
        camera1 = sm.createCamera("MainCamera1", Projection.PERSPECTIVE);
        rw.getViewport(0).setCamera(camera1);
        camera1.setRt((Vector3f) Vector3f.createFrom(1.0f, 0.0f, 0.0f));
        camera1.setUp((Vector3f) Vector3f.createFrom(0.0f, 1.0f, 0.0f));
        camera1.setFd((Vector3f) Vector3f.createFrom(0.0f, 0.0f, -1.0f));
        camera1.setPo((Vector3f) Vector3f.createFrom(0.0f, 0.0f, 0.0f));
        SceneNode cameraN1 = rootNode.createChildSceneNode("MainCamera1Node");
        cameraN1.attachObject(camera1);
        camera1.setMode('n');
        camera1.getFrustum().setFarClipDistance(1000.0f);

        /*
        camera2 = sm.createCamera("MainCamera2",Projection.PERSPECTIVE);
        rw.getViewport(1).setCamera(camera2);
        camera2.setRt((Vector3f)Vector3f.createFrom(1.0f, 0.0f, 0.0f));
        camera2.setUp((Vector3f)Vector3f.createFrom(0.0f, 1.0f, 0.0f));
        camera2.setFd((Vector3f)Vector3f.createFrom(0.0f, 0.0f, -1.0f));
        camera2.setPo((Vector3f)Vector3f.createFrom(0.0f, 0.0f, 0.0f));
        SceneNode cameraN2 = rootNode.createChildSceneNode("MainCamera2Node");
        cameraN2.attachObject(camera2);
        camera2.setMode('n');
        camera2.getFrustum().setFarClipDistance(1000.0f);
        */
    }

    @Override
    protected void setupScene(Engine eng, SceneManager sm) throws IOException {
        setDefaults();


        // set up sky box
        Configuration conf = eng.getConfiguration();
        TextureManager txm = getEngine().getTextureManager();
        //txm.setBaseDirectoryPath(conf.valueOf("assets.skyboxes.path"));
        txm.setBaseDirectoryPath("assets/skyboxes/alienSky/");
        Texture front = txm.getAssetByPath("AlienSky3_LeftHalf.png");
        Texture back = txm.getAssetByPath("AlienSky3_LeftHalf.png");
        Texture left = txm.getAssetByPath("AlienSky3_RightHalf.png");
        Texture right = txm.getAssetByPath("AlienSky3_RightHalf.png");
        Texture top = txm.getAssetByPath("AlienSky3_Top.png");
        Texture bottom = txm.getAssetByPath("AlienSky3_Bottom.png");
        txm.setBaseDirectoryPath(conf.valueOf("assets.textures.path"));

        // cubemap textures are flipped upside-down.
        // All  textures must have the same dimensions, so any image’s
        // heights will work since they are all the same height

        AffineTransform xform = new AffineTransform();
        xform.translate(0, front.getImage().getHeight());
        xform.scale(1d, -1d);

        front.transform(xform);
        back.transform(xform);
        left.transform(xform);
        right.transform(xform);
        top.transform(xform);
        bottom.transform(xform);

        SkyBox sb = sm.createSkyBox(SKYBOX_NAME);
        sb.setTexture(front, SkyBox.Face.FRONT);
        sb.setTexture(back, SkyBox.Face.BACK);
        sb.setTexture(left, SkyBox.Face.LEFT);
        sb.setTexture(right, SkyBox.Face.RIGHT);
        sb.setTexture(top, SkyBox.Face.TOP);
        sb.setTexture(bottom, SkyBox.Face.BOTTOM);
        sm.setActiveSkyBox(sb);


        // create Player 1 dolphin
//
//        Entity player1E = sm.createEntity("player1E", playerModel);
//        player1E.setPrimitive(Primitive.TRIANGLES);
        TextureManager tm = eng.getTextureManager();
//        Texture moonTexture = tm.getAssetByPath(playerTexture);
//        RenderSystem rs = sm.getRenderSystem();
//        TextureState state = (TextureState) rs.createRenderState(RenderState.Type.TEXTURE);
//        state.setTexture(moonTexture);
//        player1E.setRenderState(state);
//        Material mat1 = sm.getMaterialManager().getAssetByPath("cone.mtl");
//        mat1.setShininess(100);
//
//        player1E.setMaterial(mat1);
//
//        player1Node = sm.getRootSceneNode().createChildSceneNode("player1Node");
//        player1Node.moveBackward(5.0f);
//        player1Node.moveRight(2f);
//        player1Node.attachObject(player1E);

        //animation
        if (networkType.compareTo("c") == 0 || networkType.compareTo("m") == 0) {
            SkeletalEntity player1E = sm.createSkeletalEntity("player1E", playerModel, playerSkeleton);
            Texture tex = sm.getTextureManager().getAssetByPath(playerTexture);
            TextureState tstate = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
            tstate.setTexture(tex);
            player1E.setRenderState(tstate);
// attach the entity to a scene node
            player1Node = sm.getRootSceneNode().createChildSceneNode("player1Node");
//            player1Node.moveBackward(5.0f);
//            player1Node.moveRight(2f);

            player1Node.setLocalPosition(Vector3f.createFrom(26.0f, 1.65f, -28.0f));
            player1Node.attachObject(player1E);
            player1Node.scale(0.2f, 0.2f, 0.2f);


// load animations
            if(playerTexture.contains("cTxt")){
                player1E.loadAnimation("danceAnimation", "dance2.rka");

            }
        }


//            if (playerModel.contains("robot"))
//                player1Node.scale(0.25f, 0.25f, 0.25f);
//        }




        /*
        // create Player 2 dolphin
        Entity player2E = sm.createEntity("player2E", "dolphinHighPoly.obj");
        player2E.setPrimitive(Primitive.TRIANGLES);
        player2Node = sm.getRootSceneNode().createChildSceneNode("player2Node");
        player2Node.moveBackward(5.0f);
        player2Node.moveLeft(2f);
        player2Node.attachObject(player2E);
        // set render state for Player 2
        TextureManager tm = eng.getTextureManager();
        Texture redTexture = tm.getAssetByPath("blue-snow.jpeg");
        RenderSystem rs = sm.getRenderSystem();
        TextureState state = (TextureState)rs.createRenderState(RenderState.Type.TEXTURE);
        state.setTexture(redTexture);
        player2E.setRenderState(state);
        */


        // set up lights
        //sm.getAmbientLight().setIntensity(new Color(.1f, .1f, .1f));

        Light plight1 = sm.createLight("Lamp1", Light.Type.POINT);
        plight1.setAmbient(new Color(.1f, .1f, .1f));
        plight1.setDiffuse(new Color(.1f, .1f, .1f));
        plight1.setSpecular(new Color(0.1f, 0.1f, 0.1f));
        plight1.setRange(50f);
        SceneNode plightNode1 = sm.getRootSceneNode().createChildSceneNode("plightNode1");
        plightNode1.attachObject(plight1);
        plightNode1.moveUp(10f);

//        Light plight2 = sm.createLight("Lamp2", Light.Type.POINT);
//        plight2.setAmbient(new Color(.3f, .3f, .3f));
//        plight2.setDiffuse(new Color(.7f, .7f, .7f));
//        plight2.setSpecular(new Color(0.0f, 1.0f, 1.0f));
//        plight2.setRange(50f);
//        SceneNode plightNode2 = sm.getRootSceneNode().createChildSceneNode("plightNode2");
//        plightNode2.attachObject(plight2);
//        plightNode2.moveForward(20f);
//        //plightNode2.moveLeft(20f);
//        plightNode2.moveUp(10f);


        if (networkType.equals("c")) {
            spotlight = sm.createLight("spotlight", Light.Type.SPOT);
            //spotlight.setAmbient(new Color(0.1f, 0.1f, 0.1f));
            spotlight.setDiffuse(new Color(1.0f, 1.0f, 1.0f));
            spotlight.setSpecular(new Color(1.0f, 1.0f, 1.0f));
            spotlight.setConeCutoffAngle(Degreef.createFrom(30.0f));
            //spotlight.setConstantAttenuation(0.4f);
            spotlight.setLinearAttenuation(0.01f);
            //spotlight.setQuadraticAttenuation(0.01f);
            spotlight.setFalloffExponent(0.1f);
            spotlight.setRange(100f);
            SceneNode spotLightNode = sm.getSceneNode("player1Node").createChildSceneNode("spotLightNode"); //getSceneNode("player1Node")
            spotLightNode.attachObject(spotlight);
            spotLightNode.pitch(Degreef.createFrom(10.0f));
            System.out.println(spotLightNode.getWorldPosition());
            spotLightNode.moveUp(2f);
        }

        //Vector3f spotlightPos = (Vector3f) spotLightNode.getWorldPosition();
        //spotLightNode.setLocalPosition(spotlightPos.x(), spotlightPos.y()*-1, spotlightPos.z());


        // set up node controllers. DO NOT change this order of adding them to the scene. Needed for PlanetVisited()
        RotationController rc = new RotationController(Vector3f.createUnitVectorY(), .05f);
        sm.addController(rc);
        BounceController bc = new BounceController(); // user-defined node controller
        sm.addController(bc);
        setupNetworking(); //setup network
        setupInputs();
        setupOrbitCamera(eng, sm);

        //ISSUE: camera also moves up and down with the playerNode. Need to fix later
        HoverController hc = new HoverController(); // makes player appear as if they are hovering
        if (networkType.equals("c") || networkType.compareTo("m") == 0) {
            sm.addController(hc);
            hc.addNode(player1Node);
        }


        // code for adding terrain
        // 2^patches: min=5, def=7, warnings start at 10
        Tessellation tessE = sm.createTessellation("tessE", 6);

        // subdivisions per patch:  min=0, try up to 32
        tessE.setSubdivisions(8f);

        SceneNode tessN = sm.getRootSceneNode().createChildSceneNode("tessN");
        tessN.attachObject(tessE);

        // to move it, note that X and Z must BOTH be positive OR negative
        // tessN.translate(Vector3f.createFrom(-6.2f, -2.2f, 2.7f));
        // tessN.yaw(Degreef.createFrom(37.2f));

        tessN.scale(60, 80, 60);
        tessE.setHeightMap(this.getEngine(), "terrainMap4.png");
        //assets/scripts/" + scriptFileName
        tessE.setTexture(this.getEngine(), "bottom.jpg");
        // tessE.setNormalMap(. . .)



        /*
        // add maze object
        Entity mazeE = sm.createEntity("mazeE", "maze1.obj");
        mazeE.setPrimitive(Primitive.TRIANGLES);
        SceneNode mazeNode = sm.getRootSceneNode().createChildSceneNode("mazeNode");
        mazeNode.attachObject(mazeE);
        mazeNode.scale(1f, .10f, 1f);
        */


        if (networkType.equals("c") || networkType.compareTo("m") == 0) {
            updateVerticalPosition(); // make sure player is above the terrain when game loads
        }


///*
        //TESTING
        SceneNode rootNode = sm.getRootSceneNode();


        /*
        // Ball 1
        Entity ball1Entity = sm.createEntity("ball1", "earth.obj");
        ball1Node = rootNode.createChildSceneNode("Ball1Node");
        ball1Node.attachObject(ball1Entity);
        ball1Node.setLocalPosition(0, 0, 0);
        //ball1Node.setLocalPosition(0,-20,0);
        */

        ///*
        // Ball 2
        Entity ball2Entity = sm.createEntity("Ball2", "sphere.obj");
        ball2Node = rootNode.createChildSceneNode("Ball2Node");
        ball2Node.attachObject(ball2Entity);
        //ball2Node.scale(0.4f, 0.4f, 0.4f);
        ball2Node.setLocalPosition(-1, 5, -2);


        //tm = getEngine().getTextureManager();
        //txm.setBaseDirectoryPath(conf.valueOf("assets.skyboxes.path"));
        //tm.setBaseDirectoryPath("assets/textures/");

        //TextureManager tm2 = eng.getTextureManager();
        Texture redTexture = eng.getTextureManager().getAssetByPath("ground.jpeg");
        RenderSystem rs2 = sm.getRenderSystem();
        TextureState state2 = (TextureState) rs2.createRenderState(RenderState.Type.TEXTURE);
        state2.setTexture(redTexture);
        ball2Entity.setRenderState(state2);


        // Ground plane
        Entity groundEntity = sm.createEntity(GROUND_E, "cube.obj");
        gndNode = rootNode.createChildSceneNode(GROUND_N);
        gndNode.attachObject(groundEntity);
        gndNode.setLocalPosition(0, 1, -2);

        //END of TESTING
        //
        ManualObject shape = makeShape(eng, sm);
        SceneNode shapeN = sm.getRootSceneNode().createChildSceneNode("ShapeNode");
        shapeN.scale(1.0f, 1.0f, 1.0f);
        shapeN.attachObject(shape);
        shapeN.setLocalPosition(Vector3f.createFrom(-1.05f, 5.65f, 0));
        //specialItemN.attachObject(specialItem);
        System.out.println(shapeN.getWorldPosition());
        // physics
        initPhysicsSystem();
        createRagePhysicsWorld();


        buildMaze(eng, sm);
    }


    protected void setupOrbitCamera(Engine eng, SceneManager sm) {
        //im = new GenericInputManager(); // already handled in setupInputs. Calling again here overwrites setupInput actions
        String kbName = im.getKeyboardName();
        String gpName = im.getFirstGamepadName();

        // set up for Player 1
        if (networkType.equals("c") || networkType.compareTo("m") == 0) {
            SceneNode player1N = sm.getSceneNode("player1Node");
            SceneNode camera1N = sm.getSceneNode("MainCamera1Node");
            orbitController1 = new Camera3Pcontroller(camera1, camera1N, player1N, kbName, im, rotationAmount);
        }


        /*
        if (gpName != null) // only do if there is a gamepad connected
        {
            // set up for Player 2
            SceneNode player2N = sm.getSceneNode("player2Node");
            SceneNode camera2N = sm.getSceneNode("MainCamera2Node");
            orbitController2 = new Camera3Pcontroller(camera2, camera2N, player2N, gpName, im, rotationAmount);
        }
        */
    }


    @Override
    protected void update(Engine engine) {

        processNetworking(elapsTime);
        if (networkType.equals("c") || networkType.compareTo("m") == 0) {
            if(playerTexture.contains("cTxt.png")){
                SkeletalEntity player1E = (SkeletalEntity) engine.getSceneManager().getEntity("player1E");
                player1E.update();
            }

            for(GhostAvatar ghost: ghostAvatars){
                ghost.getEntity().update();
            }

        }



        // if scripting is turned on, read any scripts and update appropriate variables
        if (allowJavascripts) {
            ScriptEngineManager factory = new ScriptEngineManager();
            String scriptFileName = "updateSpeed.js";
            ScriptEngine jsEngine = factory.getEngineByName("js"); // get the JavaScript engine
            movementSpeed = executeScript(jsEngine, scriptFileName, "speed"); // run the script
            playerController1.updateSpeed(movementSpeed);
        }


        // build and set HUD
        rs = (GL4RenderSystem) engine.getRenderSystem();

        //if (!allPlanetsVisited) // only tick clock while not game over
        elapsTime += engine.getElapsedTimeMillis();

        elapsTimeSec = Math.round(elapsTime / 1000.0f);
        elapsTimeStr = Integer.toString(elapsTimeSec);

        // get top viewport's actual left and bottom
        int view1Left = rs.getRenderWindow().getViewport(0).getActualLeft();
        int view1Bottom = rs.getRenderWindow().getViewport(0).getActualBottom();




        if(playerOneWins == false && NPCWins == false && ghostWon == false){
            player1HUD = "Time = " + elapsTimeStr + "   Score = " + player1Score + "   Javascripts Enabled = " + allowJavascripts;
            winGameTime = elapsTimeStr;
        }


        //if (allPlanetsVisited)
        //    player1HUD += "   GAME OVER";
        rs.setHUD(player1HUD, view1Left + 15, view1Bottom + 15);

        //System.out.println(specialItemN.getWorldPosition());
        /*
        if (orbitController2 != null)
        {
            player2HUD = "Time = " + elapsTimeStr + "   Planets Visited = " + player2PlanetsVisited + " / " + totalPlanets + "   Score = " + player2Score;
            if (allPlanetsVisited)
                player2HUD += "   GAME OVER";
        }
        else
            player2HUD = "No controller is connected";
		rs.setHUD2(player2HUD, 15, 15);
        */

        im.update(elapsTime); // tell the input manager to process the inputs
        if (networkType.compareTo("c") == 0 || networkType.compareTo("m") == 0) {
            orbitController1.updateCameraPosition();
        }
        if(networkType.equals("c")){
            for(GhostAvatar ghost: ghostAvatars){
                if(ghost.getNode().getWorldPosition().x() < 4.30f && ghost.getNode().getWorldPosition().x() > -4.30f && ghost.getNode().getWorldPosition().z() < 3.4f && ghost.getNode().getWorldPosition().z() > -3.4f && NPCWins == false && playerOneWins == false){

                    if(gameOver == false){
                        currentTimeStr = elapsTimeStr;
                    }
                    ghostWon = true;
                    gameOver = true;
                    player1HUD = "Maze Time = " + currentTimeStr + "   Score = 100"  + " Another player won, better luck next time" +"   Javascripts Enabled = " + allowJavascripts;

                }
            }

            SceneNode player = engine.getSceneManager().getSceneNode("player1Node");
            SceneNode npc = null;
            if(engine.getSceneManager().hasSceneNode("npc0")){
                npc = engine.getSceneManager().getSceneNode("npc0");
            }
            if(player.getWorldPosition().x() < 4.30f && player.getWorldPosition().x() > -4.30f && player.getWorldPosition().z() < 3.4f && player.getWorldPosition().z() > -3.4f && NPCWins == false && ghostWon == false){
                if(gameOver == false){
                    currentTimeStr = elapsTimeStr;
                }
                playerOneWins = true;
                gameOver = true;
                protClient.sendWinMessage(currentTimeStr);
                player1HUD = "Maze Time = " + currentTimeStr + "   Score = 100"  + " congratulations, you won!" +"   Javascripts Enabled = " + allowJavascripts;
            }
            if(npc!= null){
                if(gameOver == false){
                    currentTimeStr = elapsTimeStr;
                }
                if(npc.getWorldPosition().x() < 4.30f && npc.getWorldPosition().x() > -4.30f&& npc.getWorldPosition().z() < 3.4f && npc.getWorldPosition().z() > -3.4f && playerOneWins == false && ghostWon == false){
                    NPCWins = true;
                    gameOver = true;
                    player1HUD = "Maze Time = " + currentTimeStr + "   Score = 100"  + " The NPC won, better luck next time" +"   Javascripts Enabled = " + allowJavascripts;
                }

            }

        }


///*
        // physics
        float time = engine.getElapsedTimeMillis();

        Matrix4 mat;
        physicsEng.update(time);

        for (SceneNode s : engine.getSceneManager().getSceneNodes()) {
            if (s.getPhysicsObject() != null) {
                mat = Matrix4f.createFrom(toFloatArray(s.getPhysicsObject().getTransform()));
                s.setLocalPosition(mat.value(0, 3), mat.value(1, 3), mat.value(2, 3));
            }
        }


        // reset ball
        Vector3 vf = ball2Node.getLocalPosition();
        if (vf.x() >= 50 || vf.y() >= 50 || vf.y() >= 50)
            ball2Node.setLocalPosition(-1, 5, -2);

        ball2PhysObj.applyForce(10, 0, 0, 0, 0, 0);
        ball2PhysObj.applyTorque(100, 1000, 0); // doesn't seem to have any effect

        // */

    }


    protected void setupInputs() {
        im = new GenericInputManager();
        String kbName = im.getKeyboardName();
        //System.out.println("keyboard name: " + kbName);
        String gpName = im.getFirstGamepadName();
        //System.out.println("gamepad name: " + gpName);

        // set up for Player 1
        if (networkType.compareTo("c") == 0) {
            playerController1 = new PlayerController(player1Node, kbName, im, movementSpeed, protClient, game);
            System.out.println("client movement setup");
        } else if (!(networkType.compareTo("c") == 0) && !(networkType.compareTo("s") == 0)) {
            playerController1 = new PlayerController(player1Node, kbName, im, movementSpeed, game);
            //physController1 = new PlayerController(gndNode, kbName, im, movementSpeed, game);
            //physController1 = new PlayerController(ball2Node, kbName, im, movementSpeed, game);
        }


//        if (gpName != null) // only do if there is a gamepad connected
//        {
//            if (networkType.compareTo("c") == 0) {
//                playerController2 = new PlayerController(player2Node, gpName, im, movementSpeed, protClient, game);
//            }
//            playerController2 = new PlayerController(player2Node, gpName, im, movementSpeed, game);
//        }

        // Set up additional inputs below

        // list of key names
        //http://lagers.org.uk/gamecontrol/ref/classnet_1_1java_1_1games_1_1input_1_1_component_1_1_identifier_1_1_key.html
        quitGameAction = new QuitGameAction(this);
        im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.ESCAPE, quitGameAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

        toggleJSAction = new ToggleJSAction(this);
        im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.SPACE, toggleJSAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
    }


    protected void planetVisited(SceneNode playerNode, SceneNode planetNode) {
        if (playerNode.getName().contains("1")) {
            player1Score += scoreIncrement; // increase score
            player1PlanetsVisited++; // increment count of planets visited
            BounceController bc = (BounceController) getEngine().getSceneManager().getController(1); // gets the bounceController
            bc.addNode(planetNode); // add Node controller to planet
        } else {
            player2Score += scoreIncrement; // increase score
            player2PlanetsVisited++; // increment count of planets visited
            RotationController rc = (RotationController) getEngine().getSceneManager().getController(0); // gets the RotationController
            rc.addNode(planetNode); // add Node controller to planet
        }
    }


    protected ManualObject makeGroundPlane(Engine eng, SceneManager sm) throws IOException {
        ManualObject groundPlane = sm.createManualObject("GroundPlane");
        ManualObjectSection groundPlaneSec = groundPlane.createManualSection("GroundPlaneSection");
        groundPlane.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));

        float[] vertices = new float[]
                {
                        -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f,
                        1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f,
                };

        float[] texcoords = new float[]
                {
                        1.0f, 0.0f, 1.0f, 1.0f, 0.5f, 1.0f,
                        0.0f, 0.0f, 0.0f, 1.0f, 0.5f, 1.0f,
                };

        float[] normals = new float[]
                {
                        0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
                        0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f
                };

        int[] indices = new int[]{0, 1, 2, 3, 4, 5, 6};//,7,8,9,10,11,12,13,14,15,16,17 };

        FloatBuffer vertBuf = BufferUtil.directFloatBuffer(vertices);
        FloatBuffer texBuf = BufferUtil.directFloatBuffer(texcoords);
        FloatBuffer normBuf = BufferUtil.directFloatBuffer(normals);
        IntBuffer indexBuf = BufferUtil.directIntBuffer(indices);

        groundPlaneSec.setVertexBuffer(vertBuf);
        groundPlaneSec.setTextureCoordsBuffer(texBuf);
        groundPlaneSec.setNormalsBuffer(normBuf);
        groundPlaneSec.setIndexBuffer(indexBuf);

        Texture tex = eng.getTextureManager().getAssetByPath("bottom.jpg");
        TextureState texState = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
        texState.setTexture(tex);
        FrontFaceState faceState = (FrontFaceState) sm.getRenderSystem().createRenderState(RenderState.Type.FRONT_FACE);
        groundPlane.setDataSource(DataSource.INDEX_BUFFER);
        groundPlane.setRenderState(texState);
        groundPlane.setRenderState(faceState);

        return groundPlane;
    }


    private float getDistance(Vector3 obj1, Vector3 obj2) {
        // calculates and returns the distance between two vectors
        Vector3 dLoc = obj1;
        Vector3 cLoc = obj2;

        float dx = dLoc.x();
        float dy = dLoc.y();
        float dz = dLoc.z();

        float cx = cLoc.x();
        float cy = cLoc.y();
        float cz = cLoc.z();

        float distance = ((dx - cx) * (dx - cx)) + ((dy - cy) * (dy - cy)) + ((dz - cz) * (dz - cz));
        return distance;
    }


    private void executeScript(ScriptEngine engine, String scriptFileName) {
        try {
            FileReader fileReader = new FileReader("assets/scripts/" + scriptFileName);
            engine.eval(fileReader);    //execute the script statements in the file
            fileReader.close();
        } catch (FileNotFoundException e1) {
            System.out.println(scriptFileName + " not found " + e1);
        } catch (IOException e2) {
            System.out.println("IO problem with " + scriptFileName + e2);
        } catch (ScriptException e3) {
            System.out.println("ScriptException in " + scriptFileName + e3);
        } catch (NullPointerException e4) {
            System.out.println("Null ptr exception in " + scriptFileName + e4);
        }
    }

    private float executeScript(ScriptEngine engine, String scriptFileName, String varName) {
        float var = 0;
        try {
            FileReader fileReader = new FileReader("assets/scripts/" + scriptFileName);
            engine.eval(fileReader);    //execute the script statements in the file
            fileReader.close();
            var = (((Double) (engine.get(varName))).floatValue());
        } catch (FileNotFoundException e1) {
            System.out.println(scriptFileName + " not found " + e1);
        } catch (IOException e2) {
            System.out.println("IO problem with " + scriptFileName + e2);
        } catch (ScriptException e3) {
            System.out.println("ScriptException in " + scriptFileName + e3);
        } catch (NullPointerException e4) {
            System.out.println("Null ptr exception in " + scriptFileName + e4);
        }

        return var;
    }

    private void setDefaults() {
        // reset any values changed by the javascripts to their default values
        scoreIncrement = 100;
        movementSpeed = 0.08f;
        rotationAmount = 1.0f;
    }

    public void toggleJS() {
        if (allowJavascripts) {
            allowJavascripts = false;
            setDefaults();
            playerController1.updateSpeed(movementSpeed); // at this time only movement speed needs to be updated
        } else
            allowJavascripts = true;
    }


//networking

    private void setupNetworking() {
        gameObjectsToRemove = new LinkedList<UUID>();
        if (networkType.compareTo("s") == 0) { //server
            //System.out.println("you are in single player mode");
            try {
                thisUDPServer = new GameServerUDP(serverPort, npcCtrl, this);
                //  npcCtrl = new NPCcontroller(thisUDPServer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (networkType.compareTo("c") == 0) { //client
            isClientConnected = true;
            try {
                protClient = new ProtocolClient(InetAddress.getByName(serverAddress), serverPort, serverProtocol, this);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (protClient == null) {
                System.out.println("missing protocol host");
            } else { // ask client protocol to send initial join message
                ///to server, with a unique identifier for this client
                protClient.sendJoinMessage();
                Vector3f testPosVector = (Vector3f) Vector3f.createFrom(0.0f, 0.0f, 0.0f);
                // protClient.sendCreateMessage(testPosVector);
            }
        }

    }

    protected void processNetworking(float elapsTime) { // Process packets received by the client from the server
        if (protClient != null)
            protClient.processPackets();
// remove ghost avatars for players who have left the game
        Iterator<UUID> it = gameObjectsToRemove.iterator();
        while (it.hasNext()) {
            getEngine().getSceneManager().destroySceneNode(it.next().toString());
        }
        gameObjectsToRemove.clear();
        for (UUID u : gameObjectsToRemove) {
        }
    }

    public Vector3 getPlayerPosition() {
        SceneNode dolphinN = getEngine().getSceneManager().getSceneNode("player1Node");
        return dolphinN.getWorldPosition();
    }

    public Quaternion getPlayerRotation() {
        SceneNode dolphinN = getEngine().getSceneManager().getSceneNode("player1Node");
        return dolphinN.getWorldRotation().toQuaternion();
    }

    public String getPlayerModel() {
        return playerModel;
    }


    public void addGhostAvatarToGameWorld(GhostAvatar avatar, String model) throws IOException {
        if (avatar != null) {
            System.out.println(model);
            //Entity ghostE = getEngine().getSceneManager().createEntity("ghost" + avatar.getId().toString(), model);
            String ghostSkeleton = "";
            String ghostTexture = "";
            if(model.equals("robo.rkm")){
                ghostSkeleton = "robo.rks";
                ghostTexture = "cTxt.png";
            }else if(model.equals("robo2.rkm")){
                ghostSkeleton = "robo2.rks";
                ghostTexture = "robot.png";
            }

            SkeletalEntity ghostE = getEngine().getSceneManager().createSkeletalEntity("ghost" + avatar.getId().toString(), model, ghostSkeleton);
            Texture tex = getEngine().getSceneManager().getTextureManager().getAssetByPath(ghostTexture);
            TextureState tstate = (TextureState) getEngine().getSceneManager().getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
            tstate.setTexture(tex);
            ghostE.setRenderState(tstate);
            ghostE.setPrimitive(Primitive.TRIANGLES);
            SceneNode ghostN = getEngine().getSceneManager().getRootSceneNode().createChildSceneNode(avatar.getId().toString());
            ghostN.attachObject(ghostE);
            ghostN.setLocalPosition(avatar.getGhostPosition()); //get position that was sent
            ghostN.setLocalRotation(avatar.getGhostRotation());
            avatar.setNode(ghostN);
            avatar.setEntity(ghostE);
            avatar.setGhostTexture(ghostTexture);
            ghostAvatars.push(avatar); // Used in update to tell if ghost wins or not
            // avatar.setPosition(node’s position...maybe redundant);
            ghostN.scale(0.2f, 0.2f, 0.2f);


        }
    }


//    public void updateGhostAvatarPosition(GhostAvatar avatar, Vector3f newPosition){
//        if(avatar!= null){
//            avatar.setLocalPosition(newPosition);
//        }
//    }


    public void removeGhostAvatarFromGameWorld(GhostAvatar avatar) {
        if (avatar != null) gameObjectsToRemove.add(avatar.getId());
        for (UUID u : gameObjectsToRemove) {
            System.out.println("client: " + u + " left");
        }
    }

    public void addGhostNPCtoGameWorld(GhostNPC npc) throws IOException {
        if (npc != null) {
            Entity npcE = getEngine().getSceneManager().createEntity("npc" + Integer.toString(npc.getId()), "dolphinHighPoly.obj");
            npcE.setPrimitive(Primitive.TRIANGLES);
            SceneNode npcN = getEngine().getSceneManager().getRootSceneNode().createChildSceneNode("npc"+Integer.toString(npc.getId()));
            npcN.attachObject(npcE);
            npcN.setLocalPosition(npc.getPosition()); //get position that was sent
            //npcN.setLocalRotation(npc.getGhostRotation());
            npc.setEntity(npcE);
            npc.setNode(npcN);
            Quaternion npcRot = npcN.getLocalRotation().toQuaternion();
            System.out.println("npc original rotation Quaternion " + npcRot);
        }


    }

    private class SendCloseConnectionPacketAction extends AbstractInputAction { // for leaving the game... need to attach to an input device
        @Override
        public void performAction(float time, Event evt) {
            if (protClient != null && isClientConnected == true) {
                protClient.sendByeMessage();
            }
        }

    }


    public void updateVerticalPosition() {
        //SceneNode dolphinN = this.getEngine().getSceneManager().getSceneNode("dolphinNode");
        SceneNode tessN = this.getEngine().getSceneManager().getSceneNode("tessN");
        Tessellation tessE = ((Tessellation) tessN.getAttachedObject("tessE"));

        // Figure out Avatar's position relative to plane
        Vector3 worldAvatarPosition = player1Node.getWorldPosition();
        Vector3 localAvatarPosition = player1Node.getLocalPosition();

        // use avatar World coordinates to get coordinates for height
        // Keep the X coordinate
        // The Y coordinate is the varying height
        // Keep the Z coordinate
        Vector3 newAvatarPosition = Vector3f.createFrom(localAvatarPosition.x(),
                tessE.getWorldHeight(worldAvatarPosition.x(), worldAvatarPosition.z()) + 0.5f,
                localAvatarPosition.z());

        // use avatar Local coordinates to set position, including height
        player1Node.setLocalPosition(newAvatarPosition);
    }

    public Engine getMyEngine() {
        return myEngine;
    }

    private void initPhysicsSystem() {
        String engine = "ray.physics.JBullet.JBulletPhysicsEngine";
        float[] gravity = {0, -3f, 0};

        physicsEng = PhysicsEngineFactory.createPhysicsEngine(engine);
        physicsEng.initSystem();
        physicsEng.setGravity(gravity);
    }


    private void createRagePhysicsWorld() {
        float mass = 10.0f;
        float up[] = {0, 1, 0};
        double[] temptf;


        /*
        ball1Node.scale(.4f, .4f, .4f);
        //ball1Node.setLocalPosition(0,-10,0); // gone completely if negative value
        temptf = toDoubleArray(ball1Node.getLocalTransform().toFloatArray());
        ball1PhysObj = physicsEng.addSphereObject(physicsEng.nextUID(),mass, temptf, 2.0f);
        //ball1PhysObj.setBounciness(0f);
        ball1Node.setPhysicsObject(ball1PhysObj);
        */

        ///*
        temptf = toDoubleArray(ball2Node.getLocalTransform().toFloatArray());
        ball2PhysObj = physicsEng.addSphereObject(physicsEng.nextUID(), mass, temptf, 2.0f);
        ball2PhysObj.setBounciness(1.0f);
        ball2Node.setPhysicsObject(ball2PhysObj);
        //*/

        temptf = toDoubleArray(gndNode.getLocalTransform().toFloatArray());
        gndPlaneP = physicsEng.addStaticPlaneObject(physicsEng.nextUID(), temptf, up, 0.0f);
        //gndPlaneP.setBounciness(0f);
        gndNode.scale(3f, .05f, 3f);
        gndNode.setLocalPosition(0, -2, -2);
        gndNode.setPhysicsObject(gndPlaneP);

        // can also set damping, friction, etc.
        //ball2PhysObj.applyTorque(1000,0,0);
        //ball2PhysObj.applyForce(500,0,0,0,0,0);
    }

    private float[] toFloatArray(double[] arr) {
        if (arr == null) return null;
        int n = arr.length;
        float[] ret = new float[n];

        for (int i = 0; i < n; i++) {
            ret[i] = (float) arr[i];
        }

        return ret;
    }

    private double[] toDoubleArray(float[] arr) {
        if (arr == null)
            return null;

        int n = arr.length;
        double[] ret = new double[n];

        for (int i = 0; i < n; i++) {
            ret[i] = (double) arr[i];
        }

        return ret;
    }


    private void buildMaze(Engine eng, SceneManager sm) throws IOException {
        // build maze out of 22 cube/rectangle objects
        // use a scene node hierarchy to group them

        /*
        ------------------------------------
        |
        |  -------------------------------
        |  |
        |  |  |---------   --------------
        |     |  ______________________
        |  |  |  |
        |  |  |   _____________________
        |  |  |---------   --------------
        |  |
        |  -------------------------------
        |
        ------------------------------------
        */

        /* To keep track of naming conventions for the pieces:
        outerlevel = 4 rectangles
            outerLevelL, outerLevelR, outerLevelT, outerLevelB // outer level left, right, top, bottom
        innerLevel1 = 2 pieces of 3 rectangles each
            innerLevel1TopPieceL, innerLevel1TopPieceM, innerLevel1TopPieceR // top piece left, middle, and right
            innerLevel1BottomPieceL, innerLevel1BottomPieceM, innerLevel1BottomPieceR
        innerLevel2 = 2 pieces of 3 rectangles each
            innerLevel2LeftPieceT, innerLevel2LeftPieceM, innerLevel2LeftPieceB // left piece top, middle, and bottom
            innerLevel2RightPieceT, innerLevel2RightPieceM, innerLevel2RightPieceB
        innerLevel3 = 2 pieces of 3 rectangles each
            innerLevel3TopPieceL, innerLevel3TopPieceM, innerLevel3TopPieceR // top piece left, middle, and right
            innerLevel3BottomPieceL, innerLevel3BottomPieceM, innerLevel3BottomPieceR
        */

        // parent node that will contain all the planets
        wholeMazeNode = sm.getRootSceneNode().createChildSceneNode("wholeMazeNode");
        SceneNode outerLevelNode = wholeMazeNode.createChildSceneNode("outerLevelNode");
        SceneNode innerLevel1Node = wholeMazeNode.createChildSceneNode("innerLevel1Node");
        SceneNode innerLevel2Node = wholeMazeNode.createChildSceneNode("innerLevel2Node");
        SceneNode innerLevel3Node = wholeMazeNode.createChildSceneNode("innerLevel3Node");


        String objName = "cube.obj";

        // variables used for scaling
        float MfwdPos = 7.5f; // distance the innermost middle piece is from origin, used for forward/backward
        float Mscale = 8f;
        float LRfwdPos = 4.5f; // distance the innermost side pieces are from origin, used for forward/backward
        float LRpos = 7.0f; // distance the innermost side pieces are from origin, used for left/right
        float LRscale = 2.5f;

        float height = 1.0f;
        float UpPos = 1.0f; // used for raising/lowering height of maze


        // set up innerLevel3 top piece
        Entity innerLevel3TopPieceMEntity = sm.createEntity("innerLevel3TopPieceMEntity", objName);
        innerLevel3TopPieceMEntity.setPrimitive(Primitive.TRIANGLES);
        SceneNode innerLevel3TopPieceMNode = innerLevel3Node.createChildSceneNode("innerLevel3TopPieceMNode");
        innerLevel3TopPieceMNode.attachObject(innerLevel3TopPieceMEntity);
        innerLevel3TopPieceMNode.moveForward(MfwdPos);
        innerLevel3TopPieceMNode.scale(Mscale,1f,1f);

        Entity innerLevel3TopPieceLEntity = sm.createEntity("innerLevel3TopPieceLEntity", objName);
        innerLevel3TopPieceLEntity.setPrimitive(Primitive.TRIANGLES);
        SceneNode innerLevel3TopPieceLNode = innerLevel3Node.createChildSceneNode("innerLevel3TopPieceLNode");
        innerLevel3TopPieceLNode.attachObject(innerLevel3TopPieceLEntity);
        innerLevel3TopPieceLNode.moveForward(LRfwdPos);
        innerLevel3TopPieceLNode.moveLeft(LRpos);
        innerLevel3TopPieceLNode.scale(1f,1f, LRscale);

        Entity innerLevel3TopPieceREntity = sm.createEntity("innerLevel3TopPieceREntity", objName);
        innerLevel3TopPieceREntity.setPrimitive(Primitive.TRIANGLES);
        SceneNode innerLevel3TopPieceRNode = innerLevel3Node.createChildSceneNode("innerLevel3TopPieceRNode");
        innerLevel3TopPieceRNode.attachObject(innerLevel3TopPieceREntity);
        innerLevel3TopPieceRNode.moveForward(LRfwdPos);
        innerLevel3TopPieceRNode.moveRight(LRpos);
        innerLevel3TopPieceRNode.scale(1f,1f, LRscale);


        // set up innerLevel3 bottom piece
        Entity innerLevel3BottomPieceMEntity = sm.createEntity("innerLevel3BottomPieceMEntity", objName);
        innerLevel3BottomPieceMEntity.setPrimitive(Primitive.TRIANGLES);
        SceneNode innerLevel3BottomPieceMNode = innerLevel3Node.createChildSceneNode("innerLevel3BottomPieceMNode");
        innerLevel3BottomPieceMNode.attachObject(innerLevel3BottomPieceMEntity);
        innerLevel3BottomPieceMNode.moveBackward(MfwdPos);
        innerLevel3BottomPieceMNode.scale(Mscale,1f,1f);

        Entity innerLevel3BottomPieceLEntity = sm.createEntity("innerLevel3BottomPieceLEntity", objName);
        innerLevel3BottomPieceLEntity.setPrimitive(Primitive.TRIANGLES);
        SceneNode innerLevel3BottomPieceLNode = innerLevel3Node.createChildSceneNode("innerLevel3BottomPieceLNode");
        innerLevel3BottomPieceLNode.attachObject(innerLevel3BottomPieceLEntity);
        innerLevel3BottomPieceLNode.moveBackward(LRfwdPos);
        innerLevel3BottomPieceLNode.moveLeft(LRpos);
        innerLevel3BottomPieceLNode.scale(1f,1f,LRscale);

        Entity innerLevel3BottomPieceREntity = sm.createEntity("innerLevel3BottomPieceREntity", objName);
        innerLevel3BottomPieceREntity.setPrimitive(Primitive.TRIANGLES);
        SceneNode innerLevel3BottomPieceRNode = innerLevel3Node.createChildSceneNode("innerLevel3BottomPieceRNode");
        innerLevel3BottomPieceRNode.attachObject(innerLevel3BottomPieceREntity);
        innerLevel3BottomPieceRNode.moveBackward(LRfwdPos);
        innerLevel3BottomPieceRNode.moveRight(LRpos);
        innerLevel3BottomPieceRNode.scale(1f,1f,LRscale);


        // set up innerLevel2 left piece
        Entity innerLevel2LeftPieceMEntity = sm.createEntity("innerLevel2LeftPieceMEntity", objName);
        innerLevel2LeftPieceMEntity.setPrimitive(Primitive.TRIANGLES);
        SceneNode innerLevel2LeftPieceMNode = innerLevel2Node.createChildSceneNode("innerLevel2LeftPieceMNode");
        innerLevel2LeftPieceMNode.attachObject(innerLevel2LeftPieceMEntity);
        innerLevel2LeftPieceMNode.moveLeft(MfwdPos * 2);
        innerLevel2LeftPieceMNode.scale(1f,1f, Mscale * 2);

        Entity innerLevel2LeftPieceTEntity = sm.createEntity("innerLevel2LeftPieceTEntity", objName);
        innerLevel2LeftPieceTEntity.setPrimitive(Primitive.TRIANGLES);
        SceneNode innerLevel2LeftPieceTNode = innerLevel2Node.createChildSceneNode("innerLevel2LeftPieceTNode");
        innerLevel2LeftPieceTNode.attachObject(innerLevel2LeftPieceTEntity);
        innerLevel2LeftPieceTNode.moveForward(LRpos * 2 + 1);
        innerLevel2LeftPieceTNode.moveLeft(LRpos + 1);
        innerLevel2LeftPieceTNode.scale(LRscale * 2 + 1,1f,1f);

        Entity innerLevel2LeftPieceBEntity = sm.createEntity("innerLevel2LeftPieceBEntity", objName);
        innerLevel2LeftPieceBEntity.setPrimitive(Primitive.TRIANGLES);
        SceneNode innerLevel2LeftPieceBNode = innerLevel2Node.createChildSceneNode("innerLevel2LeftPieceBNode");
        innerLevel2LeftPieceBNode.attachObject(innerLevel2LeftPieceBEntity);
        innerLevel2LeftPieceBNode.moveBackward(LRpos * 2 + 1);
        innerLevel2LeftPieceBNode.moveLeft(LRpos + 1);
        innerLevel2LeftPieceBNode.scale(LRscale * 2 + 1,1f,1f);


        // set up innerLevel2 right piece
        Entity innerLevel2RightPieceMEntity = sm.createEntity("innerLevel2RightPieceMEntity", objName);
        innerLevel2RightPieceMEntity.setPrimitive(Primitive.TRIANGLES);
        SceneNode innerLevel2RightPieceMNode = innerLevel2Node.createChildSceneNode("innerLevel2RightPieceMNode");
        innerLevel2RightPieceMNode.attachObject(innerLevel2RightPieceMEntity);
        innerLevel2RightPieceMNode.moveRight(MfwdPos * 2);
        innerLevel2RightPieceMNode.scale(1f,1f, Mscale * 2);

        Entity innerLevel2RightPieceTEntity = sm.createEntity("innerLevel2RightPieceTEntity", objName);
        innerLevel2RightPieceTEntity.setPrimitive(Primitive.TRIANGLES);
        SceneNode innerLevel2RightPieceTNode = innerLevel2Node.createChildSceneNode("innerLevel2RightPieceTNode");
        innerLevel2RightPieceTNode.attachObject(innerLevel2RightPieceTEntity);
        innerLevel2RightPieceTNode.moveForward(LRpos * 2 + 1);
        innerLevel2RightPieceTNode.moveRight(LRpos + 1);
        innerLevel2RightPieceTNode.scale(LRscale * 2 + 1f,1f,1f);

        Entity innerLevel2RightPieceBEntity = sm.createEntity("innerLevel2RightPieceBEntity", objName);
        innerLevel2RightPieceBEntity.setPrimitive(Primitive.TRIANGLES);
        SceneNode innerLevel2RightPieceBNode = innerLevel2Node.createChildSceneNode("innerLevel2RightPieceBNode");
        innerLevel2RightPieceBNode.attachObject(innerLevel2RightPieceBEntity);
        innerLevel2RightPieceBNode.moveBackward(LRpos * 2 + 1);
        innerLevel2RightPieceBNode.moveRight(LRpos + 1);
        innerLevel2RightPieceBNode.scale(LRscale * 2 + 1,1f,1f);


        // set up innerLevel1 top piece
        Entity innerLevel1TopPieceMEntity = sm.createEntity("innerLevel1TopPieceMEntity", objName);
        innerLevel1TopPieceMEntity.setPrimitive(Primitive.TRIANGLES);
        SceneNode innerLevel1TopPieceMNode = innerLevel1Node.createChildSceneNode("innerLevel1TopPieceMNode");
        innerLevel1TopPieceMNode.attachObject(innerLevel1TopPieceMEntity);
        innerLevel1TopPieceMNode.moveForward(MfwdPos * 3 + 1);
        innerLevel1TopPieceMNode.scale(Mscale * 3,1f,1f);

        Entity innerLevel1TopPieceLEntity = sm.createEntity("innerLevel1TopPieceLEntity", objName);
        innerLevel1TopPieceLEntity.setPrimitive(Primitive.TRIANGLES);
        SceneNode innerLevel1TopPieceLNode = innerLevel1Node.createChildSceneNode("innerLevel1TopPieceLNode");
        innerLevel1TopPieceLNode.attachObject(innerLevel1TopPieceLEntity);
        innerLevel1TopPieceLNode.moveForward(LRfwdPos * 3);
        innerLevel1TopPieceLNode.moveLeft(LRpos * 3 + 2);
        innerLevel1TopPieceLNode.scale(1f,1f, LRscale * 4 + 1);

        Entity innerLevel1TopPieceREntity = sm.createEntity("innerLevel1TopPieceREntity", objName);
        innerLevel1TopPieceREntity.setPrimitive(Primitive.TRIANGLES);
        SceneNode innerLevel1TopPieceRNode = innerLevel1Node.createChildSceneNode("innerLevel1TopPieceRNode");
        innerLevel1TopPieceRNode.attachObject(innerLevel1TopPieceREntity);
        innerLevel1TopPieceRNode.moveForward(LRfwdPos * 3);
        innerLevel1TopPieceRNode.moveRight(LRpos * 3 + 2);
        innerLevel1TopPieceRNode.scale(1f,1f,LRscale * 4 + 1);


        // set up innerLevel1 bottom piece
        Entity innerLevel1BottomPieceMEntity = sm.createEntity("innerLevel1BottomPieceMEntity", objName);
        innerLevel1BottomPieceMEntity.setPrimitive(Primitive.TRIANGLES);
        SceneNode innerLevel1BottomPieceMNode = innerLevel1Node.createChildSceneNode("innerLevel1BottomPieceMNode");
        innerLevel1BottomPieceMNode.attachObject(innerLevel1BottomPieceMEntity);
        innerLevel1BottomPieceMNode.moveBackward(MfwdPos * 3 + 1);
        innerLevel1BottomPieceMNode.scale(Mscale * 3,1f,1f);

        Entity innerLevel1BottomPieceLEntity = sm.createEntity("innerLevel1BottomPieceLEntity", objName);
        innerLevel1BottomPieceLEntity.setPrimitive(Primitive.TRIANGLES);
        SceneNode innerLevel1BottomPieceLNode = innerLevel1Node.createChildSceneNode("innerLevel1BottomPieceLNode");
        innerLevel1BottomPieceLNode.attachObject(innerLevel1BottomPieceLEntity);
        innerLevel1BottomPieceLNode.moveBackward(LRfwdPos * 3);
        innerLevel1BottomPieceLNode.moveLeft(LRpos * 3 + 2);
        innerLevel1BottomPieceLNode.scale(1f,1f,LRscale * 4 + 1);

        Entity innerLevel1BottomPieceREntity = sm.createEntity("innerLevel1BottomPieceREntity", objName);
        innerLevel3BottomPieceREntity.setPrimitive(Primitive.TRIANGLES);
        SceneNode innerLevel1BottomPieceRNode = innerLevel1Node.createChildSceneNode("innerLevel1BottomPieceRNode");
        innerLevel1BottomPieceRNode.attachObject(innerLevel1BottomPieceREntity);
        innerLevel1BottomPieceRNode.moveBackward(LRfwdPos * 3);
        innerLevel1BottomPieceRNode.moveRight(LRpos * 3 + 2);
        innerLevel1BottomPieceRNode.scale(1f,1f,LRscale * 4 + 1);


        // set up outer level
        //outerLevelL, outerLevelR, outerLevelT, outerLevelB // outer level left, right, top, bottom
        Entity outerLevelTEntity = sm.createEntity("outerLevelTEntity", objName);
        outerLevelTEntity.setPrimitive(Primitive.TRIANGLES);
        SceneNode outerLevelTNode = outerLevelNode.createChildSceneNode("outerLevelTNode");
        outerLevelTNode.attachObject(outerLevelTEntity);
        outerLevelTNode.moveForward(MfwdPos * 4);
        outerLevelTNode.scale(Mscale * 4,1f,1f);

        Entity outerLevelBEntity = sm.createEntity("outerLevelBEntity", objName);
        outerLevelBEntity.setPrimitive(Primitive.TRIANGLES);
        SceneNode outerLevelBNode = outerLevelNode.createChildSceneNode("outerLevelBNode");
        outerLevelBNode.attachObject(outerLevelBEntity);
        outerLevelBNode.moveBackward(MfwdPos * 4);
        outerLevelBNode.scale(Mscale * 4,1f,1f);

        Entity outerLevelLEntity = sm.createEntity("outerLevelLEntity", objName);
        outerLevelLEntity.setPrimitive(Primitive.TRIANGLES);
        SceneNode outerLevelLNode = outerLevelNode.createChildSceneNode("outerLevelLNode");
        outerLevelLNode.attachObject(outerLevelLEntity);
        outerLevelLNode.moveLeft(MfwdPos * 4);
        outerLevelLNode.scale(1f,1f,Mscale * 4);

        Entity outerLevelREntity = sm.createEntity("outerLevelREntity", objName);
        outerLevelREntity.setPrimitive(Primitive.TRIANGLES);
        SceneNode outerLevelRNode = outerLevelNode.createChildSceneNode("outerLevelRNode");
        outerLevelRNode.attachObject(outerLevelREntity);
        outerLevelRNode.moveRight(MfwdPos * 4);
        outerLevelRNode.scale(1f,1f,Mscale * 4);


        // Scale height of maze. Uses hierarchy to apply it to all maze pieces
        wholeMazeNode.scale(1f, height,1f);
        wholeMazeNode.moveUp(UpPos);

        // make the outermost maze walls taller
        outerLevelNode.scale(1f, height * 2,1f);
        outerLevelNode.moveUp(UpPos * 2 - 0.5f);

        //wholeMazeNode.moveDown(20); // TEMP
    }

    public boolean checkDistanceFromWall(SceneNode obj) {
        // get player's pos
        // check if loc is inside innerLevel3
        // if yes, only compare to those wall objects

        // only need to check x and z? coords
        // maybe use gridlike system?
        // ie) player is less than 10f on x axis away from center, which means they are in checking distance for inner lev 3 and 2

        boolean tooClose = false;
        float distanceThreshold = 2f;

        Vector3 obj1 = obj.getWorldPosition();
        float x = obj1.x();
        float z = obj1.z();


        // innerLevel3 range: x = +- 8f, z = +- 7.5f (not worrying about the maze openings yet.
        // innerLevel2 range: x = 15f, z = 16f

        /*
        Cases:
        1: player inside InnerLevel3
        2: player between InnerLevel2 and 3
        3: player between InnerLevel 1 and 2
        4: player between OuterLevel and InnerLevel1
        */

        // check for case 1: player inside InnerLevel3
        if ((x > -8f && x < 8f) && (z > -8f && z < 8f))
        {
            // detailed check for innerLevel3 wall collisions
            // only 6 cube objects to check distance to
            // can narrow down even further by checking the sign of the x z values
            // this way only 2 objects to compare distance to
            // using this same method for the other levels would mean 4 objects to compare distance to
///*
            if (x > 0)
            {

                if (z < 0)
                {
                    // need lowerPiece L and M
                    SceneNode innerLevel3BottomPieceLNode = getEngine().getSceneManager().getSceneNode("innerLevel3BottomPieceLNode");
                    SceneNode innerLevel3BottomPieceMNode = getEngine().getSceneManager().getSceneNode("innerLevel3BottomPieceMNode");

                    // for M piece, just need to check z distance
                    tooClose = getZdistance(obj1, innerLevel3BottomPieceMNode.getWorldPosition(), distanceThreshold);

                    if (!tooClose) // if not already too close, check the next wall segment
                    {
                        // need to check both x and z distance
                        // first check x
                        tooClose = getXdistance(obj1, innerLevel3BottomPieceLNode.getWorldPosition(), distanceThreshold);

                        // if we are tooClose on the x axis, we need to check if we are not too close on z axis
                        // if player is past the xDistance threshold it may be because they are passing through an opening
                        if (tooClose)
                            tooClose = getZdistance(obj1, innerLevel3BottomPieceLNode.getWorldPosition(), distanceThreshold + 6);
                    }
                }

                else // z > 0
                {
                    // need upperPiece L and M
                    SceneNode innerLevel3TopPieceLNode = getEngine().getSceneManager().getSceneNode("innerLevel3TopPieceLNode");
                    SceneNode innerLevel3TopPieceMNode = getEngine().getSceneManager().getSceneNode("innerLevel3TopPieceMNode");

                    // for M piece, just need to check z distance
                    tooClose = getZdistance(obj1, innerLevel3TopPieceMNode.getWorldPosition(), distanceThreshold);

                    if (!tooClose) // if not already too close, check the next wall segment
                    {
                        tooClose = getXdistance(obj1, innerLevel3TopPieceLNode.getWorldPosition(), distanceThreshold);

                        if (tooClose)
                            tooClose = getZdistance(obj1, innerLevel3TopPieceLNode.getWorldPosition(), distanceThreshold + 6);
                    }
                }
            }

            else // x < 0
            {
                if (z < 0)
                {
                    // need lowerPiece R and M
                    SceneNode innerLevel3BottomPieceRNode = getEngine().getSceneManager().getSceneNode("innerLevel3BottomPieceRNode");
                    SceneNode innerLevel3BottomPieceMNode = getEngine().getSceneManager().getSceneNode("innerLevel3BottomPieceMNode");

                    // for M piece, just need to check z distance
                    tooClose = getZdistance(obj1, innerLevel3BottomPieceMNode.getWorldPosition(), distanceThreshold);

                    if (!tooClose) // if not already too close, check the next wall segment
                    {
                        // need to check both x and z distance
                        // first check x
                        tooClose = getXdistance(obj1, innerLevel3BottomPieceRNode.getWorldPosition(), distanceThreshold);

                        // if we are tooClose on the x axis, we need to check if we are not too close on z axis
                        // if player is past the xDistance threshold it may be because they are passing through an opening
                        if (tooClose)
                            tooClose = getZdistance(obj1, innerLevel3BottomPieceRNode.getWorldPosition(), distanceThreshold + 6);
                    }
                }

                else // z > 0
                {
                    // need UpperPiece R and M
                    SceneNode innerLevel3TopPieceRNode = getEngine().getSceneManager().getSceneNode("innerLevel3TopPieceRNode");
                    SceneNode innerLevel3TopPieceMNode = getEngine().getSceneManager().getSceneNode("innerLevel3TopPieceMNode");

                    // for M piece, just need to check z distance
                    tooClose = getZdistance(obj1, innerLevel3TopPieceMNode.getWorldPosition(), distanceThreshold);

                    if (!tooClose) // if not already too close, check the next wall segment
                    {
                        tooClose = getXdistance(obj1, innerLevel3TopPieceRNode.getWorldPosition(), distanceThreshold);

                        if (tooClose)
                            tooClose = getZdistance(obj1, innerLevel3TopPieceRNode.getWorldPosition(), distanceThreshold + 6);
                    }
                }
            }
        }

/* Code was introducing more bugs so I'm leaving it out for now
//===========================================================================================================================
        // check for case 2: player between InnerLevel2 and 3
        // player is closer to InnerLevel3 (check for distance to backside of InnerLevel3
        if ((x > -10f && x < 10f) && (z > -10f && z < 10f))
        {
            if (x > 0)
            {
                if (z < 0)
                {
                    // need lowerPiece L and M
                    SceneNode innerLevel3BottomPieceLNode = getEngine().getSceneManager().getSceneNode("innerLevel3BottomPieceLNode");
                    SceneNode innerLevel3BottomPieceMNode = getEngine().getSceneManager().getSceneNode("innerLevel3BottomPieceMNode");

                    // for M piece, just need to check z distance
                    tooClose = getZdistance(obj1, innerLevel3BottomPieceMNode.getWorldPosition(), distanceThreshold);

                    if (!tooClose) // if not already too close, check the next wall segment
                    {
                        // need to check both x and z distance
                        // first check x
                        tooClose = getXdistance(obj1, innerLevel3BottomPieceLNode.getWorldPosition(), distanceThreshold - 2);

                        // if we are tooClose on the x axis, we need to check if we are not too close on z axis
                        // if player is past the xDistance threshold it may be because they are passing through an opening
                        if (tooClose)
                            tooClose = getZdistance(obj1, innerLevel3BottomPieceLNode.getWorldPosition(), distanceThreshold + 4);
                    }
                }

                else // z > 0
                {
                    // need upperPiece L and M
                    SceneNode innerLevel3TopPieceLNode = getEngine().getSceneManager().getSceneNode("innerLevel3TopPieceLNode");
                    SceneNode innerLevel3TopPieceMNode = getEngine().getSceneManager().getSceneNode("innerLevel3TopPieceMNode");

                    // for M piece, just need to check z distance
                    tooClose = getZdistance(obj1, innerLevel3TopPieceMNode.getWorldPosition(), distanceThreshold);

                    if (!tooClose) // if not already too close, check the next wall segment
                    {
                        tooClose = getXdistance(obj1, innerLevel3TopPieceLNode.getWorldPosition(), distanceThreshold);

                        if (tooClose)
                            tooClose = getZdistance(obj1, innerLevel3TopPieceLNode.getWorldPosition(), distanceThreshold + 6);
                    }
                }
            }

            else // x < 0
            {
                if (z < 0)
                {
                    // need lowerPiece R and M
                    SceneNode innerLevel3BottomPieceRNode = getEngine().getSceneManager().getSceneNode("innerLevel3BottomPieceRNode");
                    SceneNode innerLevel3BottomPieceMNode = getEngine().getSceneManager().getSceneNode("innerLevel3BottomPieceMNode");

                    // for M piece, just need to check z distance
                    tooClose = getZdistance(obj1, innerLevel3BottomPieceMNode.getWorldPosition(), distanceThreshold);

                    if (!tooClose) // if not already too close, check the next wall segment
                    {
                        // need to check both x and z distance
                        // first check x
                        tooClose = getXdistance(obj1, innerLevel3BottomPieceRNode.getWorldPosition(), distanceThreshold);

                        // if we are tooClose on the x axis, we need to check if we are not too close on z axis
                        // if player is past the xDistance threshold it may be because they are passing through an opening
                        if (tooClose)
                            tooClose = getZdistance(obj1, innerLevel3BottomPieceRNode.getWorldPosition(), distanceThreshold + 6);
                    }
                }

                else // z > 0
                {
                    // need UpperPiece R and M
                    SceneNode innerLevel3TopPieceRNode = getEngine().getSceneManager().getSceneNode("innerLevel3TopPieceRNode");
                    SceneNode innerLevel3TopPieceMNode = getEngine().getSceneManager().getSceneNode("innerLevel3TopPieceMNode");

                    // for M piece, just need to check z distance
                    tooClose = getZdistance(obj1, innerLevel3TopPieceMNode.getWorldPosition(), distanceThreshold);

                    if (!tooClose) // if not already too close, check the next wall segment
                    {
                        tooClose = getXdistance(obj1, innerLevel3TopPieceRNode.getWorldPosition(), distanceThreshold);

                        if (tooClose)
                            tooClose = getZdistance(obj1, innerLevel3TopPieceRNode.getWorldPosition(), distanceThreshold + 6);
                    }
                }
            }
        }
  */

        // check for case 2: player between InnerLevel2 and 3
        // player is closer to InnerLevel2
        else if ((x > -16f && x < 16f) && (z > -16f && z < 16f))
        {
            if (x > 0)
            {
                if (z < 0)
                {
                    // need leftPiece B and M
                    SceneNode innerLevel2LeftPieceBNode = getEngine().getSceneManager().getSceneNode("innerLevel2LeftPieceBNode");
                    SceneNode innerLevel2LeftPieceMNode = getEngine().getSceneManager().getSceneNode("innerLevel2LeftPieceMNode");

                    // for M piece, just need to check x distance
                    tooClose = getXdistance(obj1, innerLevel2LeftPieceMNode.getWorldPosition(), distanceThreshold);

                    if (!tooClose) // if not already too close, check the next wall segment
                    {
                        // need to check both x and z distance
                        tooClose = getZdistance(obj1, innerLevel2LeftPieceBNode.getWorldPosition(), distanceThreshold);

                        if (tooClose)
                            tooClose = getXdistance(obj1, innerLevel2LeftPieceBNode.getWorldPosition(), (distanceThreshold + 18) * 2);
                    }
                }

                else // z > 0
                {
                    // need leftPiece T and M
                    SceneNode innerLevel2LeftPieceTNode = getEngine().getSceneManager().getSceneNode("innerLevel2LeftPieceTNode");
                    SceneNode innerLevel2LeftPieceMNode = getEngine().getSceneManager().getSceneNode("innerLevel2LeftPieceMNode");

                    // for M piece, just need to check z distance
                    tooClose = getXdistance(obj1, innerLevel2LeftPieceMNode.getWorldPosition(), distanceThreshold);

                    if (!tooClose) // if not already too close, check the next wall segment
                    {
                        tooClose = getZdistance(obj1, innerLevel2LeftPieceTNode.getWorldPosition(), distanceThreshold);

                        if (tooClose)
                            tooClose = getXdistance(obj1, innerLevel2LeftPieceTNode.getWorldPosition(), (distanceThreshold + 18) * 2);
                    }
                }
            }

            else // x < 0
            {
                if (z < 0)
                {
                    // need rightPiece B and M
                    SceneNode innerLevel2RightPieceBNode = getEngine().getSceneManager().getSceneNode("innerLevel2RightPieceBNode");
                    SceneNode innerLevel2RightPieceMNode = getEngine().getSceneManager().getSceneNode("innerLevel2RightPieceMNode");

                    // for M piece, just need to check z distance
                    tooClose = getXdistance(obj1, innerLevel2RightPieceMNode.getWorldPosition(), distanceThreshold);

                    if (!tooClose) // if not already too close, check the next wall segment
                    {
                        // need to check both x and z distance
                        // first check x
                        tooClose = getZdistance(obj1, innerLevel2RightPieceBNode.getWorldPosition(), distanceThreshold);

                        // if we are tooClose on the x axis, we need to check if we are not too close on z axis
                        // if player is past the xDistance threshold it may be because they are passing through an opening
                        if (tooClose)
                            tooClose = getXdistance(obj1, innerLevel2RightPieceBNode.getWorldPosition(), (distanceThreshold + 18) * 2);
                    }
                }

                else // z > 0
                {
                    // need rightPiece T and M
                    SceneNode innerLevel2RightPieceTNode = getEngine().getSceneManager().getSceneNode("innerLevel2RightPieceTNode");
                    SceneNode innerLevel2RightPieceMNode = getEngine().getSceneManager().getSceneNode("innerLevel2RightPieceMNode");

                    // for M piece, just need to check z distance
                    tooClose = getXdistance(obj1, innerLevel2RightPieceMNode.getWorldPosition(), distanceThreshold);

                    if (!tooClose) // if not already too close, check the next wall segment
                    {
                        tooClose = getZdistance(obj1, innerLevel2RightPieceTNode.getWorldPosition(), distanceThreshold);

                        if (tooClose)
                            tooClose = getXdistance(obj1, innerLevel2RightPieceTNode.getWorldPosition(), (distanceThreshold + 18) * 2);
                    }
                }
            }
            //*/
        }

///*
        // check for case 3: player between InnerLevel1 and 2
        else if ((x > -24f && x < 24f) && (z > -24f && z < 24f))
        {

            if (x > 0)
            {

                if (z < 0)
                {
                    // need lowerPiece L and M
                    SceneNode innerLevel1BottomPieceLNode = getEngine().getSceneManager().getSceneNode("innerLevel1BottomPieceLNode");
                    SceneNode innerLevel1BottomPieceMNode = getEngine().getSceneManager().getSceneNode("innerLevel1BottomPieceMNode");

                    // for M piece, just need to check z distance
                    tooClose = getZdistance(obj1, innerLevel1BottomPieceMNode.getWorldPosition(), distanceThreshold);

                    if (!tooClose) // if not already too close, check the next wall segment
                    {
                        // need to check both x and z distance
                        // first check x
                        tooClose = getXdistance(obj1, innerLevel1BottomPieceLNode.getWorldPosition(), distanceThreshold);

                        // if we are tooClose on the x axis, we need to check if we are not too close on z axis
                        // if player is past the xDistance threshold it may be because they are passing through an opening
                        if (tooClose)
                            tooClose = getZdistance(obj1, innerLevel1BottomPieceLNode.getWorldPosition(), distanceThreshold + 70);
                    }
                }

                else // z > 0
                {
                    // need upperPiece L and M
                    SceneNode innerLevel1TopPieceLNode = getEngine().getSceneManager().getSceneNode("innerLevel1TopPieceLNode");
                    SceneNode innerLevel1TopPieceMNode = getEngine().getSceneManager().getSceneNode("innerLevel1TopPieceMNode");

                    // for M piece, just need to check z distance
                    tooClose = getZdistance(obj1, innerLevel1TopPieceMNode.getWorldPosition(), distanceThreshold);

                    if (!tooClose) // if not already too close, check the next wall segment
                    {
                        tooClose = getXdistance(obj1, innerLevel1TopPieceLNode.getWorldPosition(), distanceThreshold);

                        if (tooClose)
                            tooClose = getZdistance(obj1, innerLevel1TopPieceLNode.getWorldPosition(), distanceThreshold + 70);
                    }
                }
            }

            else // x > 0
            {
                if (z < 0)
                {
                    // need lowerPiece R and M
                    SceneNode innerLevel1BottomPieceRNode = getEngine().getSceneManager().getSceneNode("innerLevel1BottomPieceRNode");
                    SceneNode innerLevel1BottomPieceMNode = getEngine().getSceneManager().getSceneNode("innerLevel1BottomPieceMNode");

                    // for M piece, just need to check z distance
                    tooClose = getZdistance(obj1, innerLevel1BottomPieceMNode.getWorldPosition(), distanceThreshold);

                    if (!tooClose) // if not already too close, check the next wall segment
                    {
                        // need to check both x and z distance
                        // first check x
                        tooClose = getXdistance(obj1, innerLevel1BottomPieceRNode.getWorldPosition(), distanceThreshold);

                        // if we are tooClose on the x axis, we need to check if we are not too close on z axis
                        // if player is past the xDistance threshold it may be because they are passing through an opening
                        if (tooClose)
                            tooClose = getZdistance(obj1, innerLevel1BottomPieceRNode.getWorldPosition(), distanceThreshold + 70);
                    }
                }

                else // z > 0
                {
                    // need UpperPiece R and M
                    SceneNode innerLevel1TopPieceRNode = getEngine().getSceneManager().getSceneNode("innerLevel1TopPieceRNode");
                    SceneNode innerLevel1TopPieceMNode = getEngine().getSceneManager().getSceneNode("innerLevel1TopPieceMNode");

                    // for M piece, just need to check z distance
                    tooClose = getZdistance(obj1, innerLevel1TopPieceMNode.getWorldPosition(), distanceThreshold);

                    if (!tooClose) // if not already too close, check the next wall segment
                    {
                        tooClose = getXdistance(obj1, innerLevel1TopPieceRNode.getWorldPosition(), distanceThreshold);

                        if (tooClose)
                            tooClose = getZdistance(obj1, innerLevel1TopPieceRNode.getWorldPosition(), distanceThreshold + 70);
                    }
                }
            }

        }
        //  */

        // check for case 4: player between OuterLevel and InnerLevel1
        else if ((x > -32f && x < 32f) && (z > -32f && z < 32f)) // this is for the outerLevel
        {

            if (x > 0)
            {

                if (z < 0)
                {
                    // need outerlevel pieces L and B
                    SceneNode outerLevelLNode = getEngine().getSceneManager().getSceneNode("outerLevelLNode");
                    SceneNode outerLevelBNode = getEngine().getSceneManager().getSceneNode("outerLevelBNode");

                    // check z distance for B piece
                    tooClose = getZdistance(obj1, outerLevelBNode.getWorldPosition(), distanceThreshold);

                    if (!tooClose) // if not already too close, check the next wall segment
                    {
                        // check x distance for L piece
                        tooClose = getXdistance(obj1, outerLevelLNode.getWorldPosition(), distanceThreshold);
                    }
                }

                else // z > 0
                {
                    // need outerlevel pieces L and T
                    SceneNode outerLevelLNode = getEngine().getSceneManager().getSceneNode("outerLevelLNode");
                    SceneNode outerLevelTNode = getEngine().getSceneManager().getSceneNode("outerLevelTNode");

                    // check z distance for T piece
                    tooClose = getZdistance(obj1, outerLevelTNode.getWorldPosition(), distanceThreshold);

                    if (!tooClose) // if not already too close, check the next wall segment
                    {
                        // check x distance for L piece
                        tooClose = getXdistance(obj1, outerLevelLNode.getWorldPosition(), distanceThreshold);
                    }
                }
            }

            else // x < 0
            {
                if (z < 0)
                {
                    // need outerlevel pieces R and B
                    SceneNode outerLevelRNode = getEngine().getSceneManager().getSceneNode("outerLevelRNode");
                    SceneNode outerLevelBNode = getEngine().getSceneManager().getSceneNode("outerLevelBNode");

                    // check z distance for B piece
                    tooClose = getZdistance(obj1, outerLevelBNode.getWorldPosition(), distanceThreshold);

                    if (!tooClose) // if not already too close, check the next wall segment
                    {
                        // check x distance for L piece
                        tooClose = getXdistance(obj1, outerLevelRNode.getWorldPosition(), distanceThreshold);
                    }
                }

                else // z > 0
                {
                    // need outerlevel pieces R and T
                    SceneNode outerLevelRNode = getEngine().getSceneManager().getSceneNode("outerLevelRNode");
                    SceneNode outerLevelTNode = getEngine().getSceneManager().getSceneNode("outerLevelTNode");

                    // check z distance for T piece
                    tooClose = getZdistance(obj1, outerLevelTNode.getWorldPosition(), distanceThreshold);

                    if (!tooClose) // if not already too close, check the next wall segment
                    {
                        // check x distance for L piece
                        tooClose = getXdistance(obj1, outerLevelRNode.getWorldPosition(), distanceThreshold);
                    }
                }
            }

        }

        return tooClose;
    }


    private boolean getZdistance(Vector3 obj1, Vector3 obj2, float distanceThreshold) {
        // checks the distance on the z axis between two vectors

        boolean tooClose = false;
        float dz = obj1.z();
        float cz = obj2.z();

        float distance = ((dz - cz) * (dz - cz));

        if (distance < distanceThreshold)
            tooClose = true;

        return tooClose;
    }

    private boolean getXdistance(Vector3 obj1, Vector3 obj2, float distanceThreshold) {
        // checks the distance on the x axis between two vectors

        boolean tooClose = false;
        float dx = -obj1.x(); // make it negative so orientation matches the maze objects
        float cx = obj2.x();

        float distance = ((dx - cx) * (dx - cx));

        if (distance < distanceThreshold)
            tooClose = true;

        return tooClose;
    }

    //used from his notes for extra activity object
    protected ManualObject makePyramid(Engine eng, SceneManager sm) throws IOException {
        ManualObject pyr = sm.createManualObject("Pyramid");
        ManualObjectSection pyrSec = pyr.createManualSection("PyramidSection");
        pyr.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
        float[] vertices = new float[]
                {-1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f, //front
                        1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f, //right
                        1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f, //back
                        -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f, //left
                        -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, //LF
                        1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f //RR
                };


        float[] texcoords = new float[]
                {0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
                        0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
                        0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
                        0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
                        0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
                        1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f
                };
        float[] normals = new float[]
                {0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
                        1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, -1.0f, 0.0f, 1.0f, -1.0f, 0.0f, 1.0f, -1.0f,
                        -1.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f,
                        0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f,
                        0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f
                };
        int[] indices = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17};

        FloatBuffer vertBuf = BufferUtil.directFloatBuffer(vertices);
        FloatBuffer texBuf = BufferUtil.directFloatBuffer(texcoords);
        FloatBuffer normBuf = BufferUtil.directFloatBuffer(normals);
        IntBuffer indexBuf = BufferUtil.directIntBuffer(indices);
        pyrSec.setVertexBuffer(vertBuf);
        pyrSec.setTextureCoordsBuffer(texBuf);
        pyrSec.setNormalsBuffer(normBuf);
        pyrSec.setIndexBuffer(indexBuf);
        Texture tex = eng.getTextureManager().getAssetByPath("chain-fence.jpeg");
        TextureState texState = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
        texState.setTexture(tex);
        FrontFaceState faceState = (FrontFaceState) sm.getRenderSystem().createRenderState(RenderState.Type.FRONT_FACE);
        pyr.setDataSource(DataSource.INDEX_BUFFER);
        pyr.setRenderState(texState);
        pyr.setRenderState(faceState);
        return pyr;
    }

    protected ManualObject makeShape(Engine eng, SceneManager sm) throws IOException {
        ManualObject shape = sm.createManualObject("Shape");
        ManualObjectSection shapeSec = shape.createManualSection("ShapeSection");
        shape.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
        //shape
        float[] vertices = new float[]
                {-1.0f, 1.0f, 0.5f, 1.0f, 1.0f, 0.5f, 0.0f, 2.0f, 0.0f,//topfront
                        1.0f, 1.0f, 0.5f, 1.0f, 1.0f, -0.5f, 0.0f, 2.0f, 0.0f,//topright
                        1.0f, 1.0f, -0.5f, -1.0f, 1.0f, -0.5f, 0.0f, 2.0f, 0.0f,//top back
                        -1.0f, 1.0f, -0.5f, -1.0f, 1.0f, 0.5f, 0.0f, 2.0f, 0.0f,//topLeft
                        1.0f, 1.0f, 0.5f, -1.0f, 1.0f, 0.5f, 0.0f, -2.0f, 0.0f,//bottomfront
                        1.0f, 1.0f, -0.5f, 1.0f, 1.0f, 0.5f, 0.0f, -2.0f, 0.0f,//bottomright
                        -1.0f, 1.0f, -0.5f, 1.0f, 1.0f, -0.5f, 0.0f, -2.0f, 0.0f,//bottom back
                        -1.0f, 1.0f, 0.5f, -1.0f, 1.0f, -0.5f, 0.0f, -2.0f, 0.0f//bottomLeft


                };
        float[] texcoords = new float[]
                {0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f, //topfront
                        0.25f, 0.0f, 0.75f, 0.0f, 0.5f, 1.0f, //topright
                        0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f, //topback
                        0.25f, 0.0f, 0.75f, 0.0f, 0.5f, 1.0f, //topleft
                        0.25f, 0.0f, 0.75f, 0.0f, 0.5f, 1.0f, //bottomfront
                        0.4f, 0.0f, 0.6f, 0.0f, 0.5f, 1.0f, //bottomright
                        0.25f, 0.0f, 0.75f, 0.0f, 0.5f, 1.0f, //bottomback
                        0.4f, 0.0f, 0.6f, 0.0f, 0.5f, 1.0f //boottomleft


                };
        float[] normals = new float[]
                {0.0f, 2.0f, 0.5f, 0.0f, 2.0f, 0.5f, 0.0f, 2.0f, 0.5f,//topfront
                        1.0f, 2.0f, 0.0f, 1.0f, 2.0f, 0.0f, 1.0f, 2.0f, 0.0f,//topright
                        0.0f, 2.0f, -0.5f, 0.0f, 2.0f, -0.5f, 0.0f, 2.0f, -0.5f,//topback
                        -1.0f, 2.0f, 0.0f, -1.0f, 2.0f, 0.0f, -1.0f, 2.0f, 0.0f, //topleft
                        0.0f, 0.0f, 0.5f, 0.0f, 0.0f, 0.5f, 0.0f, 0.0f, 0.5f,//bottomfront
                        1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,//bottomright
                        0.0f, 0.0f, -0.5f, 0.0f, 0.0f, -0.5f, 0.0f, 0.0f, -0.5f,//bottomback
                        -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f //bottomleft


                };
        int[] indices = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23};//,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41};

        FloatBuffer vertBuf = BufferUtil.directFloatBuffer(vertices);
        FloatBuffer texBuf = BufferUtil.directFloatBuffer(texcoords);
        FloatBuffer normBuf = BufferUtil.directFloatBuffer(normals);
        IntBuffer indexBuf = BufferUtil.directIntBuffer(indices);
        shapeSec.setVertexBuffer(vertBuf);
        shapeSec.setTextureCoordsBuffer(texBuf);
        shapeSec.setNormalsBuffer(normBuf);
        shapeSec.setIndexBuffer(indexBuf);
        Texture tex = eng.getTextureManager().getAssetByPath("hexagons.jpeg");
        TextureState texState = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
        texState.setTexture(tex);
        Material mat1 = sm.getMaterialManager().getAssetByPath("moon.mtl");
        mat1.setEmissive(Color.WHITE);
        shapeSec.setMaterial(mat1);
        FrontFaceState faceState = (FrontFaceState) sm.getRenderSystem().createRenderState(RenderState.Type.FRONT_FACE);
        shape.setDataSource(DataSource.INDEX_BUFFER);
        shape.setRenderState(texState);
        shape.setRenderState(faceState);
        shape.setMaterial(mat1);
        return shape;
    }
    public void setGhostWonTrue (String time){
        currentTimeStr = time;
        if(playerOneWins == false && NPCWins == false && gameLoaded == true){

            //gameOver = true;
            ghostWon = true;
        }
    }
    public Light getSpotLight(){
        return spotlight;
    }


}