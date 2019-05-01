package networking;

import myGame.MyGame;
import ray.rage.rendersystem.Renderable;
import ray.rage.scene.Entity;
import ray.rage.scene.SceneNode;
import ray.rml.Degreef;
import ray.rml.Matrix3;
import ray.rml.Quaternion;
import ray.rml.Vector3f;

import java.io.IOException;


public class NPC {

    private float locX, locY, locZ; // other state info goes here (FSM)
    private Quaternion quaternionNPCRot;
    private Matrix3 ghostRotation;
    private SceneNode NPCnode;
    private MyGame game;
    private static int numNPCs = 0;
    public NPC(MyGame game){
        locX = 25.0f; //-2
        locY = 1.65f;
        locZ = -28.0f; //2
        this.game = game;
        try {
            setupNPC();
        } catch (IOException e) {
            e.printStackTrace();
        }
        numNPCs++;

    }
    public float getX() { return NPCnode.getWorldPosition().x(); }
    public float getY() { return NPCnode.getWorldPosition().y(); }
    public float getZ() { return NPCnode.getWorldPosition().z(); }

    //only used on netwrokng side, not in behavior tree
    public void updateLocation() {
        locX += 0.01f;
    } //System.out.println("Trying to update NPC position in npc"); }
    public void sideOneTurn(){
        System.out.println("side one turn called");
        Vector3f currentPosition = (Vector3f) NPCnode.getWorldPosition();
        locZ += 0.2f;
        NPCnode.setLocalPosition(currentPosition.x(), currentPosition.y(), locZ);


    }
    public void sideOneRotation(){
        ghostRotation = NPCnode.getWorldRotation();
        NPCnode.yaw((Degreef.createFrom(-90.0f)));
        ghostRotation = NPCnode.getWorldRotation();
    };
    public void sideTwoTurn(){
        System.out.println("side one turn called");
        Vector3f currentPosition = (Vector3f) NPCnode.getWorldPosition();
        locX -= 0.05f;
        NPCnode.setLocalPosition(locX, currentPosition.y(), currentPosition.z());
    }
    public void sideTwoRotation(){
        ghostRotation = NPCnode.getWorldRotation();
        NPCnode.yaw((Degreef.createFrom(90.0f)));
        ghostRotation = NPCnode.getWorldRotation();
    }
    public void sideThreeTurn(){
        System.out.println("Side Three turn callee");
        Vector3f currentPosition = (Vector3f) NPCnode.getWorldPosition();
        locZ += 0.2f;
        NPCnode.setLocalPosition(currentPosition.x(), currentPosition.y(), locZ);
    }
    public void sideThreeRotation(){
        ghostRotation = NPCnode.getWorldRotation();
        NPCnode.yaw((Degreef.createFrom(-90.0f)));
        ghostRotation = NPCnode.getWorldRotation();
    }
    public void sideFourTurn(){
        Vector3f currentPosition = (Vector3f) NPCnode.getWorldPosition();
        locX -= 0.2f;
        NPCnode.setLocalPosition(locX, currentPosition.y(), currentPosition.z());
    }
    public void sideFourRotation(){
        ghostRotation = NPCnode.getWorldRotation();
        NPCnode.yaw((Degreef.createFrom(-90.0f)));
        ghostRotation = NPCnode.getWorldRotation();
    }

    public Quaternion getQuaternionNPCRot(){
        ghostRotation = NPCnode.getWorldRotation();
        quaternionNPCRot = ghostRotation.toQuaternion();
        return quaternionNPCRot;
    }
    public void sideFiveTurn(){
        System.out.println("Side Five turn called");
        Vector3f currentPosition = (Vector3f) NPCnode.getWorldPosition();
        locZ -= 0.2f;
        NPCnode.setLocalPosition(currentPosition.x(), currentPosition.y(), locZ);
    }
    public void sideFiveRotation(){
        ghostRotation = NPCnode.getWorldRotation();
        NPCnode.yaw((Degreef.createFrom(90.0f)));
        ghostRotation = NPCnode.getWorldRotation();
    }
    public void sideSixTurn(){
        Vector3f currentPosition = (Vector3f) NPCnode.getWorldPosition();
        locX -= 0.2f;
        NPCnode.setLocalPosition(locX, currentPosition.y(), currentPosition.z());
    }
    public void sideSixRotation(){
        ghostRotation = NPCnode.getWorldRotation();
        NPCnode.yaw((Degreef.createFrom(-90.0f)));
        ghostRotation = NPCnode.getWorldRotation();
    }
    public void sideSevenTurn(){
        Vector3f currentPosition = (Vector3f) NPCnode.getWorldPosition();
        locZ -= 0.2f;
        NPCnode.setLocalPosition(currentPosition.x(), currentPosition.y(), locZ);
    }
    public void sideSevenRotation(){
        ghostRotation = NPCnode.getWorldRotation();
        NPCnode.yaw((Degreef.createFrom(-90.0f)));
        ghostRotation = NPCnode.getWorldRotation();
    }
    public void sideEightTurn(){
        Vector3f currentPosition = (Vector3f) NPCnode.getWorldPosition();
        locX += 0.2f;
        NPCnode.setLocalPosition(locX, currentPosition.y(), currentPosition.z());
    }

    private void setupNPC() throws IOException{
        Entity npcE = game.getEngine().getSceneManager().createEntity("npc" + numNPCs, "dolphinHighPoly.obj");

        npcE.setPrimitive(Renderable.Primitive.TRIANGLES);
        NPCnode = game.getMyEngine().getSceneManager().getRootSceneNode().createChildSceneNode("npcNode" + numNPCs);
        NPCnode.setLocalPosition(locX, locY, locZ);
        NPCnode.attachObject(npcE);
        ghostRotation = NPCnode.getWorldRotation();
        //NPCnode.yaw((Degreef.createFrom(125.0f))); //sets dolphin strait
    }
}

