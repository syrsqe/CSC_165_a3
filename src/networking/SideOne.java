package networking;

import ray.ai.behaviortrees.BTCondition;


public class SideOne extends BTCondition {
//    private GameServerUDP server;
//    private NPCcontroller npcc;
    private NPC npc;
    public SideOne(NPC n, boolean toNegate)
    {
        super(toNegate);
//        server = s;
//        npcc = c;
        npc = n;

    }

    @Override
    protected boolean check() {
         if(npc.getX() < 25.1f && npc.getX() > 24.1f && npc.getZ() < -1.50f && npc.getZ() > -28.1){
            return true;
        }
        else{
            return false;
        }
    }
}

