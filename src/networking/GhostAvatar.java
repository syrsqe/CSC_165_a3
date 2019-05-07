package networking;

import java.io.IOException;
import java.util.UUID;
import ray.rage.scene.*;
import ray.rml.Vector3f;
import ray.rml.*;

import static ray.rage.scene.SkeletalEntity.EndType.LOOP;

public class GhostAvatar {
    private UUID id;
    private SceneNode node;
    private SkeletalEntity entity;
    private Vector3f ghostPosition; //ghost position
    private Matrix3 ghostRotation;
    private boolean ghostAvatarWins, danceStarted;
    private String ghostTexture;

    public GhostAvatar(UUID id, Vector3f position, Matrix3 rotation) {

        this.id = id;
        this.ghostPosition = position;
        this.ghostRotation = rotation;
        ghostAvatarWins = false;
        danceStarted = false;
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

    public void toggleDance(){
        if(ghostTexture.contains("cTxt")){
            try {
                entity.loadAnimation("danceAnimation", "dance2.rka");
            }catch (IOException e){
                System.out.println("can't load animation in ghost Avatar: " + id);
            }

        if(danceStarted == true){
            entity.stopAnimation();
            danceStarted = false;
        }else if(danceStarted == false){
            entity.stopAnimation();
            entity.playAnimation("danceAnimation", 0.5f, LOOP, 50);
            danceStarted = true;
        }
        }

    }
    public void setGhostTexture(String text){
        ghostTexture = text;
    }
}