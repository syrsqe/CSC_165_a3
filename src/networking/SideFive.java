package networking;


import ray.ai.behaviortrees.BTCondition;

public class SideFive extends BTCondition {
    private NPC npc;

    public SideFive(NPC n, boolean toNegate) {
        super(toNegate);
//        server = s;
//        npcc = c;
        npc = n;

    }

    @Override
    protected boolean check() {
        if (npc.getX() < 0.4f && npc.getX() > -0.9f && npc.getZ() > 11.0f && npc.getZ() < 18.6f) {
            return true;

        } else {
            return false;
        }
    }
}
