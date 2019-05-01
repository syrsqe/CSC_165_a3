package networking;

import ray.ai.behaviortrees.BTStatus;
import ray.ai.behaviortrees.BTAction;


public class SideSevenTurn extends BTAction {
    private NPC npc;
    private boolean rotated = false;

    public SideSevenTurn(NPC n) {
        npc = n;
    }

    @Override
    protected BTStatus update(float v) {
        if (rotated == false) {
            npc.sideSixRotation();
            rotated = true;
        } else {
            npc.sideSevenTurn();

        }
        return BTStatus.BH_SUCCESS;
    }
}
