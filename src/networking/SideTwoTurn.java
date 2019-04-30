package networking;

import ray.ai.behaviortrees.BTStatus;
import ray.ai.behaviortrees.BTAction;


public class SideTwoTurn extends BTAction {
    private NPC npc;
    public SideTwoTurn(NPC n){
        npc = n;
    }

    @Override
    protected BTStatus update(float v) {
        npc.sideTwoTurn();
        return BTStatus.BH_SUCCESS;
    }
}
