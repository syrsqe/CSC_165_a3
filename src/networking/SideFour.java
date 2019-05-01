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
        if (npc.getX() < 19.2f && npc.getX() > 0.1f && npc.getZ() > 18.0f && npc.getZ() < 18.6f) {
            return true;
        } else {
            return false;
        }
    }
}
