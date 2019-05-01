package networking;

import ray.ai.behaviortrees.BTStatus;
import ray.ai.behaviortrees.BTAction;


public class SideSixTurn extends BTAction {
    private NPC npc;
    private boolean rotated = false;

    public SideSixTurn(NPC n) {
        npc = n;
    }

    @Override
    protected BTStatus update(float v) {
        if (rotated == false) {
            npc.sideFiveRotation();
            rotated = true;
        } else {
            npc.sideSixTurn();

        }
        return BTStatus.BH_SUCCESS;
    }
}
