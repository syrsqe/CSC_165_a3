
import java.io.IOException;
import ray.networking.IGameConnection.ProtocolType;
                        public class NetworkingServer{
                            private GameServerUDP thisUDPServer;

                            public NetworkingServer(int serverPort, String protocol) {
                                System.out.println("Got to Constructor");
                                try {
                                    if (protocol.toUpperCase().compareTo("UDP") == 0) {
                                        System.out.println("Server started on Port: " + serverPort + " using protocol:" + protocol);
                                        thisUDPServer = new GameServerUDP(serverPort);

                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            public static void main(String[] args) {
                                if (args.length > 1) {
                                   NetworkingServer app = new NetworkingServer(Integer.parseInt(args[0]), args[1]);
                                    System.out.println("Got to main");

                                }


                            }
                        }