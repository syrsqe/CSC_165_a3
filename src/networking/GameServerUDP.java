package networking;
//import networking.*;
import myGame.*;
import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;
import java.util.*;
import java.util.concurrent.*;
//import java.util.HashMap.*;
import java.lang.*;

import ray.networking.server.GameConnectionServer;
import ray.networking.server.IClientInfo;
import ray.rml.Quaternion;


public class GameServerUDP extends GameConnectionServer<UUID> {

    private ConcurrentHashMap<UUID, IClientInfo> clientList;
    private Enumeration clientEnum;
    private NPCcontroller npcCtrl;
    private MyGame game;

    public GameServerUDP(int localPort, NPCcontroller controller, MyGame game) throws IOException {
        super(localPort, ProtocolType.UDP);
        game = game;
        npcCtrl = new NPCcontroller(this, game);
        System.out.println("server running on port: " + localPort);
       // npcCtrl.start();
    }

    @Override
    public void processPacket(Object o, InetAddress senderIP, int senderPort) { // sender Ip and port provided
        String message = (String) o;
        String[] msgTokens = message.split(",");
        if (msgTokens.length > 0) {
// case where server receives a JOIN message
// format: join,localid
            if (msgTokens[0].compareTo("join") == 0) {
                try {
                    IClientInfo ci;
                    ci = getServerSocket().createClientInfo(senderIP, senderPort);
                    UUID clientID = UUID.fromString(msgTokens[1]);
                    System.out.println("client ID: " + clientID);
                    System.out.println("Client Info: " + ci);
                    addClient(ci, clientID);
                    sendJoinedMessage(clientID, true);
                    sendNPCinfoMessage(clientID);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
// case where server receives a CREATE message
// format: create,localid,x,y,z
            if (msgTokens[0].compareTo("create") == 0) {
                UUID clientID = UUID.fromString(msgTokens[1]);
                String[] pos = {msgTokens[2], msgTokens[3], msgTokens[4]};
                String[] rot = {msgTokens[5], msgTokens[6], msgTokens[7], msgTokens[8]};
                String playerModel = msgTokens[9];
                sendCreateMessages(clientID, pos, rot, playerModel);
                sendWantsDetailsForNewClientMessages(clientID);
            }
// case where server receives a BYE message
// format: bye,localid
            if (msgTokens[0].compareTo("bye") == 0) {
                UUID clientID = UUID.fromString(msgTokens[1]);
                sendByeMessages(clientID);
                removeClient(clientID);
            }
// case where server receives a DETAILS-FOR-Me-Message
            if (msgTokens[0].compareTo("dfm") == 0) {
                System.out.println("revieved details for me from client:");
                UUID destinationClientID = UUID.fromString(msgTokens[1]);
                UUID detailsClientID = UUID.fromString(msgTokens[2]);
                String[] remGhostPosition = {msgTokens[3], msgTokens[4], msgTokens[5]};
                String[] remGhostRotation = {msgTokens[6], msgTokens[7], msgTokens[8], msgTokens[9]};
                String playerModel = msgTokens[10];
                sendDetailsMessasge(detailsClientID, destinationClientID, remGhostPosition, remGhostRotation, playerModel);


            } // etc�..
// case where server receives a MOVE message

            if (msgTokens[0].compareTo("move") == 0) {
                UUID clientID = UUID.fromString(msgTokens[1]);
                String[] pos = {msgTokens[2], msgTokens[3], msgTokens[4]};
                String[] rot = {msgTokens[5], msgTokens[6], msgTokens[7], msgTokens[8]};
                sendMoveMessages(clientID, pos, rot);
            }

            if (msgTokens[0].compareTo("startNPC") == 0) {
                startNPCController();
            }
            if (msgTokens[0].compareTo("win") == 0) // rec. �move...�
            {
                String time = msgTokens[1];
                UUID clientID = UUID.fromString(msgTokens[2]);

                sendWinMessage(clientID, time);

                //find proper ghost avatar
            }
            if (msgTokens[0].compareTo("dance") == 0) // rec. �move...�
            {
                UUID clientID = UUID.fromString(msgTokens[1]);
                sendDanceMessage(clientID);

                //find proper ghost avatar
            }
        }
    }

    public void sendJoinedMessage(UUID clientID, boolean success) { // format: join, success or join, failure
        try {
            String message = new String("join,");
            if (success) message += "success";
            else message += "failure";
            sendPacket(message, clientID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendCreateMessages(UUID clientID, String[] position, String[] rotation, String model) { // format: create, remoteId, x, y, z

        String message = new String("create," + clientID.toString());
        message += "," + position[0];
        message += "," + position[1];
        message += "," + position[2];
        message += "," + rotation[0] + "," + rotation[1] + "," + rotation[2] + "," + rotation[3];
        message += "," + model;
        System.out.println("Create message recieved at server");
        clientList = getClients();
        clientEnum = clientList.keys();
        while (clientEnum.hasMoreElements()) {
            System.out.println("current Client UUID in create message: " + clientID);
            UUID nextClientID = (UUID) clientEnum.nextElement();

            if (nextClientID.compareTo(clientID) != 0) {
                try {
                    System.out.println("sending create new client to: " + nextClientID);
                    sendPacket(message, nextClientID);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //forward to new client
        }
    }

    public void sendDetailsMessasge(UUID clientID, UUID remoteID, String[] position, String[] rotation, String model) { //details for new client //remote is destination address
        String message = new String("dm," + clientID.toString()); //details for messege
        message += "," + position[0];
        message += "," + position[1];
        message += "," + position[2];
        message += "," + rotation[0] + "," + rotation[1] + "," + rotation[2] + "," + rotation[3];
        message += "," + model;
        System.out.println("sending details message to client:" + clientID);
        try {
            sendPacket(message, remoteID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }// etc�..

    public void sendWantsDetailsForNewClientMessages(UUID clientID) {
        String message = new String("wdfnc," + clientID.toString()); //has client Id to communicate with server
        //go through list and get all clients except current client
        clientList = getClients();
        clientEnum = clientList.keys();
        while (clientEnum.hasMoreElements()) {
            System.out.println("current Client UUID in wants details for new client: " + clientID);

            UUID nextClientID = (UUID) clientEnum.nextElement();

            if (nextClientID.compareTo(clientID) != 0) {
                try {
                    System.out.println("sending wants details for new client to: " + nextClientID);
                    sendPacket(message, nextClientID);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //forward to new client
        }
//        System.out.println(clientList);
//        System.out.println("sending wants details for message");

    }// etc�..

    public void sendMoveMessages(UUID clientID, String[] position, String[] rotation) {
        String message = new String("move," + clientID.toString());
        message += "," + position[0];
        message += "," + position[1];
        message += "," + position[2];
        message += "," + rotation[0] + "," + rotation[1] + "," + rotation[2] + "," + rotation[3];
        System.out.println("Movemessage recieved at server");
        clientList = getClients();
        clientEnum = clientList.keys();
        while (clientEnum.hasMoreElements()) {
            System.out.println("current Client UUID in server move message: " + clientID);
            UUID nextClientID = (UUID) clientEnum.nextElement();

            if (nextClientID.compareTo(clientID) != 0) {
                try {
                    System.out.println("sending move message to: " + nextClientID);
                    sendPacket(message, nextClientID);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //forward to new client
        }
    }// etc�..

    public void sendByeMessages(UUID clientID) {

        String message = new String("bye," + clientID.toString()); //send by message to other servers so they know to remove avatar
        System.out.println("bye message recieved at server");
        clientList = getClients();
        clientEnum = clientList.keys();
        while (clientEnum.hasMoreElements()) {
            System.out.println("sending by messege and removing clien from list: " + clientID);
            UUID nextClientID = (UUID) clientEnum.nextElement();

            if (nextClientID.compareTo(clientID) != 0) {
                try {
                    System.out.println("sending by to: " + nextClientID);
                    sendPacket(message, nextClientID);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void sendWinMessage(UUID clientID, String time){
            // System.out.println("sending move message to server");
            String message = new String("win," + time);
        clientList = getClients();
        clientEnum = clientList.keys();
        while (clientEnum.hasMoreElements()) {
            System.out.println("sending win messege for: " + clientID);
            UUID nextClientID = (UUID) clientEnum.nextElement();

            if (nextClientID.compareTo(clientID) != 0) {
                try {
                    System.out.println("sending win to: " + nextClientID);
                    sendPacket(message, nextClientID);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void sendDanceMessage(UUID clientID){
        String message = new String("dance," + clientID.toString() );
        clientList = getClients();
        clientEnum = clientList.keys();
        while (clientEnum.hasMoreElements()) {
            System.out.println("sending dance message for : " + clientID);
            UUID nextClientID = (UUID) clientEnum.nextElement();

            if (nextClientID.compareTo(clientID) != 0) {
                try {
                    System.out.println("sending dance to: " + nextClientID);
                    sendPacket(message, nextClientID);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //npc
    // sent when client joins
    public void sendNPCinfoMessage(UUID clientID) // informs clients of new NPC positions
    {
        for (int i = 0; i < npcCtrl.getNumOfNPCs(); i++) {
            try {
                String message = new String("cnpc," + Integer.toString(i));
                message += "," + (npcCtrl.getNPC(i)).getX();
                message += "," + (npcCtrl.getNPC(i)).getY();
                message += "," + (npcCtrl.getNPC(i)).getZ();
                System.out.println("sending npc info");
                sendPacket(message, clientID);
            } catch (IOException e) {
                e.printStackTrace();
            }

// . . .
//                // also additional cases for receiving messages about NPCs, such as:
//                if (messageTokens[0].compareTo("needNPC") == 0) { . . .}
//                if (messageTokens[0].compareTo("collide") == 0) { . . .}
        }

    }

    public void sendNPCUpdateinfo() {
        if(npcCtrl != null){
            for (int i = 0; i < npcCtrl.getNumOfNPCs(); i++) {
                Quaternion rot = npcCtrl.getNPC(i).getQuaternionNPCRot();
                try {
                    String message = new String("mnpc," + Integer.toString(i));
                    message += "," + (npcCtrl.getNPC(i)).getX();
                    message += "," + (npcCtrl.getNPC(i)).getY();
                    message += "," + (npcCtrl.getNPC(i)).getZ();
                    message += "," + rot.w() + "," + rot.x() + "," + rot.y()+ "," + rot.z();
                    System.out.println("sending npc update info");
                    sendPacketToAll(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    private void startNPCController(){
        if(npcCtrl.getHasStarted() == false){
            this.npcCtrl.start();
        }
        else{
            System.out.println("The NPC controller has already been started by another client");
        }

    }

}
