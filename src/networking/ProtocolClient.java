package networking;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import myGame.*;
import ray.networking.*;
import ray.networking.client.*;
import ray.rml.*;

import java.util.LinkedList;


public class ProtocolClient extends GameConnectionClient {
    private MyGame game;
    private UUID id;
    private LinkedList<GhostAvatar> ghostAvatars;
    private LinkedList<GhostNPC> ghostNPCs;

    public ProtocolClient(InetAddress remAddr, int remPort, ProtocolType pType, MyGame game) throws IOException { //initializeds in main
        super(remAddr, remPort, pType);
        this.game = game;
        this.id = UUID.randomUUID();
        ghostAvatars = new LinkedList<GhostAvatar>();
        ghostNPCs = new LinkedList<GhostNPC>();
        //this.ghostAvatars = new LinkedList<GhostAvatar>();
    }

    @Override
    protected void processPacket(Object msg) {
        String strMessage = (String) msg;
        String[] messageTokens = strMessage.split(",");
        if (messageTokens.length > 0) {
            if (messageTokens[0].compareTo("join") == 0) // receive �join�
            { // format: join, success or join, failure
                if (messageTokens[1].compareTo("success") == 0) {
                    // game.setIsConnected(true);
                    System.out.println("successfully joined game");
                    sendCreateMessage((Vector3f) game.getPlayerPosition(), game.getPlayerRotation(), game.getPlayerModel());
                }
                if (messageTokens[1].compareTo("failure") == 0) {
                    //game.setIsConnected(false);
                }
            }

            // case where client receives a DETAILS-FOR message
            if ((messageTokens[0].compareTo("dm") == 0) // receive �dsfr�
                    || (messageTokens[0].compareTo("create") == 0)) { // format: create, remoteId, x,y,z or dsfr, remoteId, x,y,z
                UUID ghostID = UUID.fromString(messageTokens[1]);
                Vector3f ghostPosition = (Vector3f) Vector3f.createFrom(Float.parseFloat(messageTokens[2]), Float.parseFloat(messageTokens[3]), Float.parseFloat(messageTokens[4]));
                Quaternion ghostRotation = Quaternionf.createFrom(Float.parseFloat(messageTokens[5]), Float.parseFloat(messageTokens[6]), Float.parseFloat(messageTokens[7]), Float.parseFloat(messageTokens[8]));
                String playerModel = messageTokens[9];
                System.out.println("create revieved by client");


                try {
                    createGhostAvatar(ghostID, ghostPosition, ghostRotation.toMatrix3(), playerModel);
                } catch (IOException e) {
                    System.out.println("error creating ghost avatar");
                }
            }
            if (messageTokens[0].compareTo("wdfnc") == 0) { // rec. �create�� //wants details for new client
                UUID ClientNeedsInfo = UUID.fromString(messageTokens[1]);
                System.out.println("client reveived wants details for message: " + this.id);

                sendDetailsForMeMessage(ClientNeedsInfo, (Vector3f) game.getPlayerPosition(), game.getPlayerRotation(), game.getPlayerModel()); // (sedDetailsfor)respond with game position and orientation

            }
            if (messageTokens[0].compareTo("bye") == 0) //on receieve by message, remove ghost avatar from local list and tell game to remove it also
            {
                for (GhostAvatar ghost : ghostAvatars) {
                    System.out.println("ghosts in ghostAvataraRRAY before client left" + ghost.getId().toString());
                }

                UUID ghostID = UUID.fromString(messageTokens[1]);
                for (GhostAvatar ghost : ghostAvatars) {
                    if (ghost.getId().compareTo(ghostID) == 0) {
                        ghostAvatars.remove(ghost);
                        game.removeGhostAvatarFromGameWorld(ghost);

                    }
                }
                for (GhostAvatar ghost : ghostAvatars) {
                    System.out.println("ghosts in ghostAvataraRRAY after client left" + ghost.getId().toString());
                }


            }
            if (messageTokens[0].compareTo("move") == 0) // rec. �move...�
            {
                //System.out.println("client recieved move message");
                UUID ghostID = UUID.fromString(messageTokens[1]);
                Vector3f ghostPosition = (Vector3f) Vector3f.createFrom(Float.parseFloat(messageTokens[2]), Float.parseFloat(messageTokens[3]), Float.parseFloat(messageTokens[4]));
                Quaternion ghostRotation = Quaternionf.createFrom(Float.parseFloat(messageTokens[5]), Float.parseFloat(messageTokens[6]), Float.parseFloat(messageTokens[7]), Float.parseFloat(messageTokens[8]));
                for (GhostAvatar ghost : ghostAvatars) {
                    if (ghost.getId().compareTo(ghostID) == 0) {
                        ghost.setGhostPosition(ghostPosition);
                        ghost.setGhostRotation(ghostRotation.toMatrix3());

                    }
                }

                //find proper ghost avatar
            }

            // handle updates to NPC positions
            //format: (mnpc,npcID,x,y,z)
            if (messageTokens[0].compareTo("cnpc") == 0) {
                int ghostID = Integer.parseInt(messageTokens[1]);
                Vector3 ghostPosition = Vector3f.createFrom(
                        Float.parseFloat(messageTokens[2]),
                        Float.parseFloat(messageTokens[3]),
                        Float.parseFloat(messageTokens[4]));
                try {
                    createGhostNPC(ghostID, (Vector3f) ghostPosition);
                } catch (IOException e) {
                    System.out.println("error creating npc avatar");
                }


            }
            if (messageTokens[0].compareTo("mnpc") == 0) {
                if (ghostNPCs.size() > 1) {
                    int ghostID = Integer.parseInt(messageTokens[1]);
                    Vector3 ghostPosition = Vector3f.createFrom(
                            Float.parseFloat(messageTokens[2]),
                            Float.parseFloat(messageTokens[3]),
                            Float.parseFloat(messageTokens[4]));
                    Quaternion ghostRotation = Quaternionf.createFrom(Float.parseFloat(messageTokens[5]), Float.parseFloat(messageTokens[6]), Float.parseFloat(messageTokens[7]), Float.parseFloat(messageTokens[8]));

                    for (GhostNPC ghost : ghostNPCs) {
                        if (ghost.getId() == ghostID) {
                            System.out.println("npc pos" + ghostPosition);
                            ghost.setPosition((Vector3f) ghostPosition);
                            ghost.setGhostRotation(ghostRotation.toMatrix3());

                        }
                    }
                }


            }
            if (messageTokens[0].compareTo("win") == 0) {
                String time = messageTokens[1];
                game.setGhostWonTrue(time);



            }
            if (messageTokens[0].compareTo("dance") == 0) {
                UUID ghostID = UUID.fromString(messageTokens[1]);
                for (GhostAvatar ghost : ghostAvatars) {
                    if (ghost.getId().compareTo(ghostID) == 0) {
                        ghost.toggleDance();

                    }
                }


            }
            if (messageTokens[0].compareTo("startPhysics") == 0) {
                game.setNpcStarted(true);

            }
        }

    }

    public void sendJoinMessage() // format: join, localId
    {
        try {
            sendPacket(new String("join," + id.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendCreateMessage(Vector3f pos, Quaternion rot, String model) { // format: (create, localId, x,y,z)
        try {
            String message = new String("create," + id.toString());
            message += "," + pos.x() + "," + pos.y() + "," + pos.z();
            message += "," + rot.w() + "," + rot.x() + "," + rot.y() + "," + rot.z();
            message += "," + model;
            sendPacket(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendByeMessage() {
        String message = new String("bye," + id.toString());
        System.out.println("sending Bye message");
        try {
            sendPacket(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendDetailsForMeMessage(UUID remId, Vector3f pos, Quaternion rot, String model) { // send my postion to remoteclient, //remId is destination
        try {
            System.out.println("sending details for me message to server");
            String message = new String("dfm," + remId.toString() + "," + id.toString());
            message += "," + pos.x() + "," + pos.y() + "," + pos.z();
            message += "," + rot.w() + "," + rot.x() + "," + rot.y() + "," + rot.z();
            message += "," + model;
            sendPacket(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMoveMessage(Vector3f pos, Quaternion rot) {
        try {
            // System.out.println("sending move message to server");
            String message = new String("move," + id.toString());
            message += "," + pos.x() + "," + pos.y() + "," + pos.z();
            message += "," + rot.w() + "," + rot.x() + "," + rot.y() + "," + rot.z();
            sendPacket(message);
            //System.out.println(pos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendWinMessage(String time) {
        try {
            // System.out.println("sending move message to server");
            String message = new String("win," + time + "," + id.toString());
            sendPacket(message);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendDanceMessage() {
        try {
            // System.out.println("sending move message to server");
            String message = new String("dance," + id.toString());
            sendPacket(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendStartNPCMessage() {
        try {
           // System.out.println("sending move message to server");
            String message = new String("startNPC,");
            sendPacket(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createGhostAvatar(UUID ghostID, Vector3f ghostPosition, Matrix3 ghostRotation, String playerModel) throws IOException {
        GhostAvatar newGhostAvatar = new GhostAvatar(ghostID, ghostPosition, ghostRotation);
        ghostAvatars.add(newGhostAvatar);
        game.addGhostAvatarToGameWorld(newGhostAvatar, playerModel);
        System.out.println("ghost avatar created");

    }

    //npc

    private void createGhostNPC(int id, Vector3f position) throws IOException {
        GhostNPC newNPC = new GhostNPC(id, position);
        ghostNPCs.add(newNPC);
        game.addGhostNPCtoGameWorld(newNPC);
        //System.out.println("ghost Npc created");
    }

    private void updateGhostNPC(int id, Vector3f position) {
        ghostNPCs.get(id).setPosition((Vector3f) position);
    }

    public void askForNPCinfo() {
        try {
            sendPacket(new String("needNPC," + id.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}


