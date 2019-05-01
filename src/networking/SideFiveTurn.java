package networking;

import ray.ai.behaviortrees.BTStatus;
import ray.ai.behaviortrees.BTAction;


public class SideFiveTurn extends BTAction {
    private NPC npc;
    private boolean rotated = false;

    public SideFiveTurn(NPC n) {
        npc = n;
    }

    @Override
    protected BTStatus update(float v) {
        if (rotated == false) {
            npc.sideFourRotation();
            rotated = true;
        } else {
            npc.sideFiveTurn();

        }
        return BTStatus.BH_SUCCESS;
    }
}
