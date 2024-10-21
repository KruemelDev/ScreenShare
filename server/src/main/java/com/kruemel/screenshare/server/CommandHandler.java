package com.kruemel.screenshare.server;

import java.io.EOFException;
import java.io.IOException;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.kruemel.screenshare.dto.Packet;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CommandHandler {
    public ClientData client;
    public CommandHandler(ClientData client){
        this.client = client;
    }

    public void HandleCommands(ClientData client){
        String message;
        Packet packet;
        while(true){
            try {
                message = client.in.readUTF();
            }
            catch (SocketException e){
                break;
            }
            catch (EOFException e){
                ConnectionHandler.removeClient(client);
                break;
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
            ObjectMapper objectMapper = new ObjectMapper();

            try {
                packet = objectMapper.readValue(message, Packet.class);
            } catch (JsonProcessingException e) {
                Packet closePacket = new Packet("closeConnection");
                ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                String json;
                try {
                    json = ow.writeValueAsString(closePacket);
                    this.client.WriteMessage(json);
                } catch (JsonProcessingException ex) {
                    throw new RuntimeException(ex);
                }

                throw new RuntimeException(e);
            }
            switch (packet.getCommand()){
                case "getClients":
                    SendAvailableClients();
                    break;
                case "closeConnection":
                    ConnectionHandler.removeClient(client);
                    return;
                case "requestScreenShare":
                    String target = packet.getData();
                    ClientData clientData = getClientByName(target);
                    if(clientData != null){
                        screenShareRequest(clientData, client.name);
                    }
                    break;
                case "acceptScreenShare":
                    ClientData allowedClient = getClientByName(packet.getData());
                    if(allowedClient == null){
                        break;
                    }
                    if(this.client.screenShareAllowed.contains(allowedClient)){
                        break;
                    }
                    System.out.println("allowed client " + allowedClient.name);
                    allowedClient.currentScreenWatching = this.client;
                    this.client.screenShareAllowed.add(allowedClient);

                    break;
                case "getScreen":
                    String base64ImagePiece = packet.getData();
                    if(base64ImagePiece.equals("fullImage")){
                        sendScreen(this.client.base64ImagePiece);
                        this.client.base64ImagePiece = "";
                    }
                    else{
                        this.client.base64ImagePiece += base64ImagePiece;
                    }
                    break;
                case "stopScreen":
                    System.out.println("this.client " + this.client.name);
                    if(this.client.currentScreenWatching == null){
                        break;
                    }
                    this.client.currentScreenWatching.screenShareAllowed.remove(this.client);

                    if (this.client.currentScreenWatching.screenShareAllowed.isEmpty()) {

                        Packet stopScreenSharePacket = new Packet("sharedScreenStop");
                        String json;
                        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                        try {
                            json = ow.writeValueAsString(stopScreenSharePacket);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                        this.client.currentScreenWatching.WriteMessage(json);

                    }

                    this.client.currentScreenWatching = null;
                    break;
            }
        }

    }

    private void sendScreen(String base64Image) {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        int chunkSize = 1024;
        String json;
        ArrayList<ClientData> clientsToRemove = new ArrayList<>();

        for (ClientData client : this.client.screenShareAllowed) {

            if(!ConnectionHandler.ClientOnline(client.name)){
                clientsToRemove.add(client);
                continue;
            }
            for (int i = 0; i < base64Image.length(); i += chunkSize) {

                String chunk = base64Image.substring(i, Math.min(i + chunkSize, base64Image.length()));
                Packet screenPacket;

                try {
                    screenPacket = new Packet("getSharedScreen", chunk);
                    json = ow.writeValueAsString(screenPacket);

                    client.WriteMessage(json);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }

            }
            Packet imageCompletePacket = new Packet("getSharedScreen", "fullImage");
            try{
                json = ow.writeValueAsString(imageCompletePacket);
            } catch(JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            client.WriteMessage(json);

        }
        for (ClientData client : clientsToRemove) {
            this.client.screenShareAllowed.remove(client);
        }
    }


    private void screenShareRequest(ClientData clientDataTarget, String requestClientName) {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json;
        if(Objects.equals(clientDataTarget.name, requestClientName)){
            Packet errorPacket = new Packet("error", "You cannot share your screen with yourself");
            try {
                json = ow.writeValueAsString(errorPacket);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            clientDataTarget.WriteMessage(json);
            return;
        }
        Packet transferScreenRequestPacket = new Packet("sharedScreenRequest", requestClientName);

        try {
            json = ow.writeValueAsString(transferScreenRequestPacket);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        clientDataTarget.WriteMessage(json);
    }
    private ClientData getClientByName(String name) {
        for(int i = 0; i < ConnectionHandler.clients.size(); i++) {
            if(Objects.equals(ConnectionHandler.clients.get(i).name, name)) {
                return ConnectionHandler.clients.get(i);
            }
        }
        return null;
    }

    public void SendAvailableClients(){
        ArrayList<String> availableClientsName = ConnectionHandler.getAvailableClientsName();
        StringBuilder message = new StringBuilder();

        for (int i = 0; i < availableClientsName.size(); i++) {
            String clientName = availableClientsName.get(i);

            message.append(clientName);

            if (i < availableClientsName.size() - 1) {
                message.append("|");
            }
        }

        String finalMessage = message.toString();

        Packet availableClientsPacket = new Packet("availableClients", finalMessage);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json;
        try {
            json = ow.writeValueAsString(availableClientsPacket);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        this.client.WriteMessage(json);
    }

}
