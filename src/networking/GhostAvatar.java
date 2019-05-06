package networking;

import java.util.UUID;
import ray.rage.scene.*;
import ray.rml.Vector3f;
import ray.rml.*;

public class GhostAvatar {
    private UUID id;
    private SceneNode node;
    private SkeletalEntity entity;
    private Vector3f ghostPosition; //ghost position
    private Matrix3 ghostRotation;
    private boolean ghostAvatarWins;

    public GhostAvatar(UUID id, Vector3f position, Matrix3 rotation) {

        this.id = id;
        this.ghostPosition = position;
        this.ghostRotation = rotation;
        ghostAvatarWins = false;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setNode(SceneNode node) {
        this.node = node;
    }

    public SceneNode getNode() {
        return node;
    }

    public void setGhostPosition(Vector3f newGhostPosition) {
        node.setLocalPosition(newGhostPosition);
        ghostPosition = newGhostPosition;
    }

    public Vector3f getGhostPosition() {
        return ghostPosition;
    }

    public void setEntity(SkeletalEntity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
    // accessors and setters for id, node, entity, and position

    public void setGhostRotation(Matrix3 ghostRotation){
        node.setLocalRotation(ghostRotation);
    }

    public Matrix3 getGhostRotation() {
        return ghostRotation;
    }

    public boolean getGhostAvatarWins(){
        return ghostAvatarWins;
    }
}