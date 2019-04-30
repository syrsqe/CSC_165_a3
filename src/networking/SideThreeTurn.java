package networking;

import ray.ai.behaviortrees.BTStatus;
import ray.ai.behaviortrees.BTAction;


public class SideThreeTurn extends BTAction {
    private NPC npc;
    public SideThreeTurn(NPC n){
        npc = n;
    }

    @Override
    protected BTStatus update(float v) {
        npc.sideThreeTurn();
        return BTStatus.BH_SUCCESS;
    }
}
