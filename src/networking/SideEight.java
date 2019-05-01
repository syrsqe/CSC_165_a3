package networking;


import ray.ai.behaviortrees.BTCondition;

public class SideEight extends BTCondition {
    private NPC npc;

    public SideEight(NPC n, boolean toNegate) {
        super(toNegate);
//        server = s;
//        npcc = c;
        npc = n;

    }

    @Override
    protected boolean check() {
        if (npc.getX() < -1.2 && npc.getX() > -11.9f && npc.getZ() > -0.2f && npc.getZ() < 0.2f) {
            return true;

        } else {
            return false;
        }
    }
}
