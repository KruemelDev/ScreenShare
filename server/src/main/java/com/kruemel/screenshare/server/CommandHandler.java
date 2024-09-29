package com.kruemel.screenshare.server;

import java.io.EOFException;
import java.io.IOException;

import java.util.ArrayList;

public class CommandHandler {
    public ConnectionHandler client;
    public CommandHandler(ConnectionHandler client){
        this.client = client;
    }

    public void HandleCommands(ClientData client){
        String message = "";
        while(true){
            try {
                message = client.in.readUTF();
            }
            catch (EOFException e){
                break;
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
            finally {
                ConnectionHandler.removeClient(client);
            }
            switch (message){
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
            System.out.println("in for");
        }

        String finalMessage = message.toString();
        System.out.println(finalMessage);

        //this.client.WriteMessage(finalMessage);
        this.client.WriteMessage("jürgen");
    }

}
