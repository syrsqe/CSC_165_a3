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
        if (npc.getX() < 25.3f && npc.getX() > 19.0 && npc.getZ() < -1.3f && npc.getZ() > -1.8f) {
            return true;
        } else {
            return false;
        }
    }
}
