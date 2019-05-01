package networking;

import ray.ai.behaviortrees.BTStatus;
import ray.ai.behaviortrees.BTAction;


public class SideEightTurn extends BTAction {
    private NPC npc;
    private boolean rotated = false;

    public SideEightTurn(NPC n) {
        npc = n;
    }

    @Override
    protected BTStatus update(float v) {
        if (rotated == false) {
            npc.sideSevenRotation();
            rotated = true;
        } else {
            npc.sideEightTurn();

        }
        return BTStatus.BH_SUCCESS;
    }
}
