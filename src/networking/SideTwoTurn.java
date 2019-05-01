package networking;

import ray.ai.behaviortrees.BTStatus;
import ray.ai.behaviortrees.BTAction;


public class SideTwoTurn extends BTAction {
    private NPC npc;
    private boolean rotated = false;
    public SideTwoTurn(NPC n){
        npc = n;
    }

    @Override
    protected BTStatus update(float v) {
        if(rotated == false){
            npc.sideOneRotation();
            rotated = true;
        }else{
            npc.sideTwoTurn();
        }


        return BTStatus.BH_SUCCESS;
    }
}
