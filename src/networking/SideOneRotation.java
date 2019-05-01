package networking;

import ray.ai.behaviortrees.BTAction;
import ray.ai.behaviortrees.BTStatus;

public class SideOneRotation extends BTAction {
    private NPC npc;
    public SideOneRotation(NPC n){
        npc = n;
    }

    @Override
    protected BTStatus update(float v) {
        npc.sideOneRotation();
        return BTStatus.BH_SUCCESS;
    }
}
