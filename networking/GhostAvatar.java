package networking;

import java.util.UUID;
import ray.rage.scene.*;
import ray.rml.Vector3f;

public class GhostAvatar {
    private UUID id;
    private SceneNode node;
    private Entity entity;
    private Vector3f ghostPosition; //ghost position

    public GhostAvatar(UUID id, Vector3f position) {

        this.id = id;
        this.ghostPosition = position;
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

    public void setGhostPosition(Vector3f ghostPosition) {
        this.ghostPosition = ghostPosition;
    }

    public Vector3f getGhostPosition() {
        return ghostPosition;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
    // accessors and setters for id, node, entity, and position
}