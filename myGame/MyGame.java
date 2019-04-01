package myGame;

import myGameEngine.*;
import networking.*;

import java.awt.*;
import java.io.*;

import ray.rage.*;
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
import ray.rage.util.BufferUtil;


import java.nio.FloatBuffer;
import java.nio.IntBuffer;

// skybox imports
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
    private PlayerController playerController1, playerController2;


    private static final String SKYBOX_NAME = "SkyBox";
    private boolean allowJavascripts = true; // javascripts can be enabled/disabled

    //Networking


    private String serverAddress;
    private int serverPort;
    private ProtocolType serverProtocol;
    private static ProtocolClient protClient;
    private GameServerUDP gameServer;
    private boolean isClientConnected;
    private LinkedList<UUID> gameObjectsToRemove;
    private static MyGame game;

    private static String networkType; //going to need to be nonestatic at some point


    private static String playerModel;


    public MyGame(String serverAddr, int sPort) {
        super();
        this.serverAddress = serverAddr;
        this.serverPort = sPort;
        this.serverProtocol = ProtocolType.UDP;
        System.out.println("PLAYER 1:");
        System.out.println("WASD keys to move");
        System.out.println("Q and E keys to turn");
        System.out.println("arrow keys to rotate the camera");
        System.out.println("Z and X keys to zoom the camera");
        System.out.println("Press SPACEBAR to toggle javascripts");

        /*
        System.out.println("PLAYER 2: (PS4 controller)");
        System.out.println("press the directional buttons to move");
        System.out.println("L1 and R1 to turn");
        System.out.println("use L2, R2, Triangle and X buttons to rotate the camera");
        System.out.println("Square and Circle buttons to zoom the camera");
        System.out.println("press ESC to quit game");
        */
    }

    public static void main(String[] args) {
        //ask about which player
        Scanner modelScanner = new Scanner(System.in);  // Create a Scanner object
        System.out.println("Choose your character:");
        System.out.println("press d for the dolphin");
        System.out.println("press c for the cone");
        String playerChoice = modelScanner.nextLine();
        if(playerChoice.equals("c")){
            playerModel = "cone.obj";

        }else if(playerChoice.equals("d")){
            playerModel = "dolphinHighPoly.obj";
        }
        game = new MyGame(args[0], Integer.parseInt(args[1]));
        networkType = args[2]; // s for server, c for client
        try {
            game.startup();
            game.run();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            game.shutdown();
            protClient.sendByeMessage();
            game.exit();
        }
    }

    @Override
    protected void setupWindow(RenderSystem rs, GraphicsEnvironment ge) {
        rs.createRenderWindow(new DisplayMode(1000, 700, 24, 60), false);
    }

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

        /*
        ScriptEngineManager factory = new ScriptEngineManager();
        String scriptFileName = "hello.js";

        // get a list of the script engines on this platform
        List<ScriptEngineFactory> list = factory.getEngineFactories();

        System.out.println("Script Engine Factories found:");
        for (ScriptEngineFactory f : list)
        {
            System.out.println("  Name = " + f.getEngineName() + "  language = " + f.getLanguageName() + "  extensions = " + f.getExtensions());
        }

        // get the JavaScript engine
        ScriptEngine jsEngine = factory.getEngineByName("js");

        // run the script
        executeScript(jsEngine, scriptFileName);
        */


        // set up sky box
        Configuration conf = eng.getConfiguration();
        TextureManager txm = getEngine().getTextureManager();
        //txm.setBaseDirectoryPath(conf.valueOf("assets.skyboxes.path"));
        txm.setBaseDirectoryPath("assets/skyboxes/grassyHill/");
        Texture front = txm.getAssetByPath("Side2.jpg");
        Texture back = txm.getAssetByPath("Side4.jpg");
        Texture left = txm.getAssetByPath("Side1.jpg");
        Texture right = txm.getAssetByPath("Side3.jpg");
        Texture top = txm.getAssetByPath("top.jpg");
        Texture bottom = txm.getAssetByPath("bottom.jpg");
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

        Entity player1E = sm.createEntity("player1E", playerModel);
        player1E.setPrimitive(Primitive.TRIANGLES);
        player1Node = sm.getRootSceneNode().createChildSceneNode("player1Node");
        player1Node.moveBackward(5.0f);
        player1Node.moveRight(2f);
        player1Node.attachObject(player1E);


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


        // set up earth
        Entity earthE = sm.createEntity("myEarth", "earth.obj");
        earthE.setPrimitive(Primitive.TRIANGLES);
        SceneNode earthN = sm.getRootSceneNode().createChildSceneNode(earthE.getName() + "Node");
        earthN.attachObject(earthE);
        earthN.moveForward(10.0f);
        earthN.moveRight(10.0f);
        //earthN.setLocalScale(0.2f, 0.2f, 0.2f);

        /*
        // create ground plane
        ManualObject groundplane = makeGroundPlane(eng, sm);
        SceneNode groundplaneN = sm.getRootSceneNode().createChildSceneNode("groundplaneN");
        groundplaneN.scale(50f, 50f, 50f);
        groundplaneN.moveUp(47.3f);
        groundplaneN.attachObject(groundplane);
        */


        // set up lights
        sm.getAmbientLight().setIntensity(new Color(.1f, .1f, .1f));

        Light plight1 = sm.createLight("Lamp1", Light.Type.POINT);
        plight1.setAmbient(new Color(.3f, .3f, .3f));
        plight1.setDiffuse(new Color(.7f, .7f, .7f));
        plight1.setSpecular(new Color(1.0f, 1.0f, 1.0f));
        plight1.setRange(50f);
        SceneNode plightNode1 = sm.getRootSceneNode().createChildSceneNode("plightNode1");
        plightNode1.attachObject(plight1);
        plightNode1.moveUp(10f);

        Light plight2 = sm.createLight("Lamp2", Light.Type.POINT);
        plight2.setAmbient(new Color(.3f, .3f, .3f));
        plight2.setDiffuse(new Color(.7f, .7f, .7f));
        plight2.setSpecular(new Color(1.0f, 1.0f, 1.0f));
        plight2.setRange(50f);
        SceneNode plightNode2 = sm.getRootSceneNode().createChildSceneNode("plightNode2");
        plightNode2.attachObject(plight2);
        plightNode2.moveForward(20f);
        //plightNode2.moveLeft(20f);
        plightNode2.moveUp(10f);


        // set up node controllers. DO NOT change this order of adding them to the scene. Needed for PlanetVisited()
        RotationController rc = new RotationController(Vector3f.createUnitVectorY(), .05f);
        sm.addController(rc);
        BounceController bc = new BounceController(); // user-defined node controller
        sm.addController(bc);
        setupNetworking(); //setup network
        setupInputs();
        setupOrbitCamera(eng, sm);


    }


    protected void setupOrbitCamera(Engine eng, SceneManager sm) {
        //im = new GenericInputManager(); // already handled in setupInputs. Calling again here overwrites setupInput actions
        String kbName = im.getKeyboardName();
        String gpName = im.getFirstGamepadName();

        // set up for Player 1
        SceneNode player1N = sm.getSceneNode("player1Node");
        SceneNode camera1N = sm.getSceneNode("MainCamera1Node");
        orbitController1 = new Camera3Pcontroller(camera1, camera1N, player1N, kbName, im, rotationAmount);

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

        player1HUD = "Time = " + elapsTimeStr + "   Score = " + player1Score + "   Javascripts Enabled = " + allowJavascripts;
        //if (allPlanetsVisited)
        //    player1HUD += "   GAME OVER";
        rs.setHUD(player1HUD, view1Left + 15, view1Bottom + 15);

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

        orbitController1.updateCameraPosition();

        /*
        if (orbitController2 != null)
            orbitController2.updateCameraPosition();
        */
    }


    protected void setupInputs() {
        im = new GenericInputManager();
        String kbName = im.getKeyboardName();
        //System.out.println("keyboard name: " + kbName);
        String gpName = im.getFirstGamepadName();
        //System.out.println("gamepad name: " + gpName);

        // set up for Player 1
        if (networkType.compareTo("c") == 0){
            playerController1 = new PlayerController(player1Node, kbName, im, movementSpeed, protClient, game);
            System.out.println("client movement setup");
        }else{
            playerController1 = new PlayerController(player1Node, kbName, im, movementSpeed, game);
        }


        if (gpName != null) // only do if there is a gamepad connected
        {
            if(networkType.compareTo("c") == 0) {
                playerController2 = new PlayerController(player2Node, gpName, im, movementSpeed, protClient, game);
            }
            playerController2 = new PlayerController(player2Node, gpName, im, movementSpeed, game);
        }

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
            try {
                gameServer = new GameServerUDP(serverPort);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (networkType.compareTo("c") == 0) { //client
            isClientConnected = false;
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
    public String getPlayerModel(){
        return playerModel;
    }


    public void addGhostAvatarToGameWorld(GhostAvatar avatar, String model) throws IOException {
        if (avatar != null) {
            Entity ghostE = getEngine().getSceneManager().createEntity("ghost" + avatar.getId().toString(), model);
            ghostE.setPrimitive(Primitive.TRIANGLES);
            SceneNode ghostN = getEngine().getSceneManager().getRootSceneNode().createChildSceneNode(avatar.getId().toString());
            ghostN.attachObject(ghostE);
            ghostN.setLocalPosition(avatar.getGhostPosition()); //get position that was sent
            ghostN.setLocalRotation(avatar.getGhostRotation());
            avatar.setNode(ghostN);
            avatar.setEntity(ghostE);
            // avatar.setPosition(node’s position...maybe redundant);


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

    private class SendCloseConnectionPacketAction extends AbstractInputAction { // for leaving the game... need to attach to an input device
        @Override
        public void performAction(float time, Event evt) {
            if (protClient != null && isClientConnected == true) {
                protClient.sendByeMessage();
            }
        }

    }
}