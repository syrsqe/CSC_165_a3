package networking;

import java.util.UUID;

import ray.rage.scene.*;
import ray.rml.Vector3f;
import ray.rml.*;


public class GhostNPC {
    private int id;
    private SceneNode node;
    private Entity entity;
    private Vector3f ghostPosition; //ghost position
    private Matrix3 ghostRotation;

    //Quaternion playerRotation = player.getWorldRotation().toQuaternion();

    public GhostNPC(int id, Vector3 position) // constructor
    {
        this.id = id;
        this.ghostPosition = (Vector3f) position;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNode(SceneNode node) {
        this.node = node;
    }

    public SceneNode getNode() {
        return node;
    }

    public void setPosition(Vector3f newGhostPosition) {
        node.setLocalPosition(newGhostPosition);
        ghostPosition = newGhostPosition;
    }

    public Vector3f getPosition() {
        return ghostPosition;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
    // accessors and setters for id, node, entity, and position

    public void setGhostRotation(Matrix3 ghostRotation) {
        node.setLocalRotation(ghostRotation);
    }

    public Matrix3 getGhostRotation() {
        return ghostRotation;
    }

}

