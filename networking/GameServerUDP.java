package networking;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;
import java.util.*;
import java.util.concurrent.*;
//import java.util.HashMap.*;
import java.lang.*;

import ray.networking.server.GameConnectionServer;
import ray.networking.server.IClientInfo;




public class GameServerUDP extends GameConnectionServer<UUID> {

    private ConcurrentHashMap<UUID, IClientInfo> clientList;
    private Enumeration clientEnum;

    public GameServerUDP(int localPort) throws IOException {
        super(localPort, ProtocolType.UDP);
        System.out.println("server running on port: " + localPort);
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
// case where server receives a CREATE message
// format: create,localid,x,y,z
            if (msgTokens[0].compareTo("create") == 0) {
                UUID clientID = UUID.fromString(msgTokens[1]);
                String[] pos = {msgTokens[2], msgTokens[3], msgTokens[4]};
                sendCreateMessages(clientID, pos);
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
                sendDetailsMessasge(detailsClientID, destinationClientID, remGhostPosition);


            } // etc�..
// case where server receives a MOVE message

            if (msgTokens[0].compareTo("move") == 0) {
            } // etc�..
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

    public void sendCreateMessages(UUID clientID, String[] position) { // format: create, remoteId, x, y, z

        String message = new String("create," + clientID.toString());
        message += "," + position[0];
        message += "," + position[1];
        message += "," + position[2];
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

    public void sendDetailsMessasge(UUID clientID, UUID remoteID, String[] position) { //details for new client //remote is destination address
        String message = new String("dm," + clientID.toString()); //details for messege
        message += "," + position[0];
        message += "," + position[1];
        message += "," + position[2];
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

    public void sendMoveMessages(UUID clientID, String[] position) {
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
}