package networking;

import ray.ai.behaviortrees.BTStatus;
import ray.ai.behaviortrees.BTAction;


public class SideThreeTurn extends BTAction {
    private NPC npc;
    private boolean rotated = false;

    public SideThreeTurn(NPC n) {
        npc = n;
    }

    @Override
    protected BTStatus update(float v) {
        if (rotated == false) {
            npc.sideTwoRotation();
            rotated = true;
        } else {
            npc.sideThreeTurn();

        }
        return BTStatus.BH_SUCCESS;
    }
}
