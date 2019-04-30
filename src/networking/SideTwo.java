package networking;

import ray.ai.behaviortrees.BTCondition;

public class SideTwo extends BTCondition {
    private NPC npc;

    public SideTwo(NPC n, boolean toNegate) {
        super(toNegate);
//        server = s;
//        npcc = c;
        npc = n;

    }

    @Override
    protected boolean check() {
        if (npc.getX() < 2.02f && npc.getX() > 1.98 && npc.getZ() < 2.01f && npc.getZ() > -2.01) {
            return true;
        } else {
            return false;
        }
    }
}
