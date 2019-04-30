package networking;

import ray.rml.Matrix3;
import ray.rml.Quaternion;


public class NPC {

    double locX, locY, locZ; // other state info goes here (FSM)
    private Quaternion quaternionNPCRot;
    private Matrix3 ghostRotation;
    public NPC(){
        locX = -2.0;
        locY = 1.5;
        locZ = 2.0;
    }
    public double getX() { return locX; }
    public double getY() { return locY; }
    public double getZ() { return locZ; }

    //only used on netwrokng side, not in behavior tree
    public void updateLocation() {
        locX += 0.01f;
    } //System.out.println("Trying to update NPC position in npc"); }
    public void sideOneTurn(){
        System.out.println("side one turn called");

        locX += 0.01f;
    }
    public void sideTwoTurn(){
        System.out.println("side one turn called");

        locZ -= 0.01f;
    }
    public void sideThreeTurn(){
        System.out.println("side one turn called");

        locX -= 0.01f;
    }
    public void sideFourTurn(){
        System.out.println("side one turn called");

        locZ += 0.01f;
    }
    public Quaternion getQuaternionNPCRot(){
        quaternionNPCRot = ghostRotation.toQuaternion();
        return getQuaternionNPCRot();
    }
}

