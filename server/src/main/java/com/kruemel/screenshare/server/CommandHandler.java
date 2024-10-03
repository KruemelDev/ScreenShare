package com.kruemel.screenshare.server;

import java.io.EOFException;
import java.io.IOException;

import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.kruemel.screenshare.dto.Packet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class CommandHandler {
    public ConnectionHandler client;
    public CommandHandler(ConnectionHandler client){
        this.client = client;
    }

    public void HandleCommands(ClientData client){
        String message;
        Packet packet;
        while(true){
            try {
                message = client.in.readUTF();
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
            }
        }


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
        System.out.println(finalMessage);


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
