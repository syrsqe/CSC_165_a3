package networking;

import ray.ai.behaviortrees.BTAction;
import ray.ai.behaviortrees.BTStatus;

public class SideOneTurn extends BTAction {
    private NPC npc;
    public SideOneTurn(NPC n){
        npc = n;
    }

    @Override
    protected BTStatus update(float v) {
        npc.sideOneTurn();
        return BTStatus.BH_SUCCESS;
    }
}
