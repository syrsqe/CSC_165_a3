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
        if (npc.getX() < 19.2f && npc.getX() > 17.9f && npc.getZ() > -1.7f && npc.getZ() < 18.0f) {
            return true;
        } else {
            return false;
        }
    }
}
