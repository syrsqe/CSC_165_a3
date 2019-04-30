package networking;


import ray.ai.behaviortrees.BTCondition;

public class SideThree extends BTCondition {
    private NPC npc;

    public SideThree(NPC n, boolean toNegate) {
        super(toNegate);
//        server = s;
//        npcc = c;
        npc = n;

    }

    @Override
    protected boolean check() {
        if (npc.getX() < 2.03f && npc.getX() > -2.0 && npc.getZ() < -1.98f && npc.getZ() > -2.03) {
            return true;
        } else {
            return false;
        }
    }
}
