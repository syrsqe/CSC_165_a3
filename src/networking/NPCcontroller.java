package networking;

import ray.ai.behaviortrees.*;
import myGame.*;


public class NPCcontroller {
    private NPC[] NPClist = new NPC[2];
    private int numNPCs = 2;
    private long thinkStartTime, tickStartTime, lastThinkUpdateTime, lastTickUpdateTime;
    private long startTime, lastUpdateTime;
    private BehaviorTree bt = new BehaviorTree(BTCompositeType.SELECTOR);
    private GameServerUDP server;
    private boolean hasStarted;
    private MyGame game;

    public NPCcontroller(GameServerUDP s, MyGame game){
        NPClist[0] = new NPC(game);
        NPClist[1] = new NPC(game);
        server = s;
        hasStarted = false;
        game = game;

    }
    public void start ()
    { thinkStartTime = System.nanoTime();
        tickStartTime = System.nanoTime();
        lastThinkUpdateTime = thinkStartTime;
        lastTickUpdateTime = tickStartTime ;
        startTime = System.nanoTime();
        lastUpdateTime = startTime;
        hasStarted = true;
        //server = server.getServer(); //get static server

        setupNPCs();
        setupBehaviorTree();
        npcLoop();
    }


    public void npcLoop()
    { while (true)
    { long currentTime = System.nanoTime();
        long frameStartTime = System.nanoTime();
        float elapMilSecs = (frameStartTime-lastUpdateTime)/(1000000.0f);
        float elapsedThinkMilliSecs = (currentTime-lastThinkUpdateTime)/(1000000.0f);
        float elapsedTickMilliSecs = (currentTime-lastTickUpdateTime)/(1000000.0f);
        if (elapsedTickMilliSecs >= 50.0f) // “TICK”
        { lastTickUpdateTime = currentTime;
            lastUpdateTime = frameStartTime;
            //updateNPCs();
            server.sendNPCUpdateinfo();
        }
        if (elapsedThinkMilliSecs >= 50.0f) // “THINK”
        { lastThinkUpdateTime = currentTime;
            bt.update(elapMilSecs);
        }
        Thread.yield();
    } }

    public void setupBehaviorTree()
    {
    bt.insertAtRoot(new BTSequence(10));
        bt.insertAtRoot(new BTSequence(20));
        bt.insertAtRoot(new BTSequence(30));
        bt.insertAtRoot(new BTSequence(40));
        bt.insertAtRoot(new BTSequence(50));
        bt.insertAtRoot(new BTSequence(60));
        bt.insertAtRoot(new BTSequence(70));
        bt.insertAtRoot(new BTSequence(80));
        bt.insert(10, new SideOne(NPClist[0], false));
        bt.insert(10, new SideOneTurn(NPClist[0]));
        bt.insert(20, new SideTwo(NPClist[0], false));
        bt.insert(20, new SideTwoTurn(NPClist[0]));
        bt.insert(30, new SideThree(NPClist[0], false));
        bt.insert(30, new SideThreeTurn(NPClist[0]));
        bt.insert(40, new SideFour(NPClist[0], false));
        bt.insert(40, new SideFourTurn(NPClist[0]));
        bt.insert(50, new SideFive(NPClist[0], false));
        bt.insert(50, new SideFiveTurn(NPClist[0]));
        bt.insert(60, new SideSix(NPClist[0], false));
        bt.insert(60, new SideSixTurn(NPClist[0]));
        bt.insert(70, new SideSeven(NPClist[0], false));
        bt.insert(70, new SideSevenTurn(NPClist[0]));
        bt.insert(80, new SideEight(NPClist[0], false));
        bt.insert(80, new SideEightTurn(NPClist[0]));


    }




    public void setupNPCs(){
        //System.out.println("Tring to set up NPCs");
        // still need to do something
    }

    public void updateNPCs()
    { for (int i=0; i< numNPCs; i++)
    { //NPClist[i].updateLocation();
   // System.out.println("updating NPC loacation");
    } }
    public int getNumOfNPCs(){
        return numNPCs;
    }
    public NPC getNPC(int index){
        return NPClist[index];
    }

    public void setHasStarted(boolean hasStarted) {
        this.hasStarted = hasStarted;
    }
    public boolean getHasStarted(){
        return hasStarted;
    }
}
