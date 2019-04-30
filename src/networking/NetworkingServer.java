package networking;
import java.io.IOException;
import ray.networking.IGameConnection.ProtocolType;
                        public class NetworkingServer{
                            private GameServerUDP thisUDPServer;
                            private NPCcontroller npcCtrl;
                            private long startTime, lastUpdateTime;

                            public NetworkingServer(int serverPort, String protocol) {
                                startTime = System.nanoTime();
                                lastUpdateTime = startTime;


//                                try {
//                                    if (protocol.toUpperCase().compareTo("UDP") == 0) {
//
//
//                                       // thisUDPServer = new GameServerUDP(serverPort, npcCtrl);
//                                      //  npcCtrl = new NPCcontroller(thisUDPServer);
//
//                                    }
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
                                //npcCtrl.start();

                                //npcCtrl.setupNPCs();
                               //npcCtrl.npcLoop();
                            }

//                            public void npcLoop() // NPC control loop
//                            { while (true)
//                            { long frameStartTime = System.nanoTime();
//                                float elapMilSecs = (frameStartTime-lastUpdateTime)/(1000000.0f);
//                                //System.out.println(elapMilSecs);
//                                if (elapMilSecs >= 50.0f)
//                                { lastUpdateTime = frameStartTime;
//                                    npcCtrl.updateNPCs();
//                                    thisUDPServer.sendNPCUpdateinfo(); //used for updating npc
//                                }
//                                Thread.yield();
//                            } }
                            public static void main(String[] args) {
                                if (args.length > 1) {
                                   NetworkingServer app = new NetworkingServer(Integer.parseInt(args[0]), args[1]);


                                }
                                while(true){
                                  //run server
                                }


                            }
                        }