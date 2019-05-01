package networking;

import ray.ai.behaviortrees.BTCondition;


public class SideOneRotCond extends BTCondition {
    //    private GameServerUDP server;
//    private NPCcontroller npcc;
    private NPC npc;
    public SideOneRotCond(NPC n, boolean toNegate)
    {
        super(toNegate);
//        server = s;
//        npcc = c;
        npc = n;

    }

    @Override
    protected boolean check() {
        if(npc.getX() < 25.1f && npc.getX() > 24.1f && npc.getZ() > -1.51f && npc.getZ() < -1.43f){
            return true;
        }
        else{
            return false;
        }
    }
}
