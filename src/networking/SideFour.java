package networking;

import ray.ai.behaviortrees.BTCondition;

public class SideFour extends BTCondition {
    private NPC npc;

    public SideFour(NPC n, boolean toNegate) {
        super(toNegate);
//        server = s;
//        npcc = c;
        npc = n;

    }

    @Override
    protected boolean check() {
        if (npc.getX() > -2.02f && npc.getX() < -1.98 && npc.getZ() < 2.0f && npc.getZ() > -2.03) {
            return true;
        } else {
            return false;
        }
    }
}
