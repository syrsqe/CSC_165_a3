package networking;


import ray.ai.behaviortrees.BTCondition;

public class SideSix extends BTCondition {
    private NPC npc;

    public SideSix(NPC n, boolean toNegate) {
        super(toNegate);
//        server = s;
//        npcc = c;
        npc = n;

    }

    @Override
    protected boolean check() {
        if (npc.getX() < 0.1f && npc.getX() > -11.2f && npc.getZ() > 10.4f && npc.getZ() < 11.0f) {
            return true;

        } else {
            return false;
        }
    }
}
