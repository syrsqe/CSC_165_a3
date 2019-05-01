package networking;

import ray.ai.behaviortrees.BTStatus;
import ray.ai.behaviortrees.BTAction;


public class SideFourTurn extends BTAction {
    private NPC npc;
    private boolean rotated = false;

    public SideFourTurn(NPC n) {
        npc = n;
    }

    @Override
    protected BTStatus update(float v) {
        if (rotated == false) {
            npc.sideThreeRotation();
            rotated = true;
        } else {
            npc.sideFourTurn();

        }
        return BTStatus.BH_SUCCESS;
    }
}
