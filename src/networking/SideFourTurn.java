package networking;

import ray.ai.behaviortrees.BTStatus;
import ray.ai.behaviortrees.BTAction;


public class SideFourTurn extends BTAction {
    private NPC npc;
    public SideFourTurn(NPC n){
        npc = n;
    }

    @Override
    protected BTStatus update(float v) {
        npc.sideFourTurn();
        return BTStatus.BH_SUCCESS;
    }
}
