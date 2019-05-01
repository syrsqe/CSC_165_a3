package networking;


import ray.ai.behaviortrees.BTCondition;

public class SideSeven extends BTCondition {
    private NPC npc;

    public SideSeven(NPC n, boolean toNegate) {
        super(toNegate);
//        server = s;
//        npcc = c;
        npc = n;

    }

    @Override
    protected boolean check() {
        if (npc.getX() < -11.1f && npc.getX() > -11.4f && npc.getZ() > .04f && npc.getZ() < 11.0f) {
            return true;

        } else {
            return false;
        }
    }
}
