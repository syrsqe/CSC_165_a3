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
         if(npc.getX() < 2.01f && npc.getX() > -2.03 && npc.getZ() < 2.01f && npc.getZ() > 1.98){
            return true;
        }
        else{
            return false;
        }
    }
}

