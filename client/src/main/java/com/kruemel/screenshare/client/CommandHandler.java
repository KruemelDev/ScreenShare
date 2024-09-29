package com.kruemel.screenshare.client;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kruemel.screenshare.dto.Packet;

public class CommandHandler implements Runnable {

    @Override
    public void run() {
        String message;
        Packet packet;
        while (true) {
            try {
                message = ConnectionHandler.instance.in.readUTF();

            } catch (EOFException e){
                WindowManager.resetToConnectMenu();
                break;
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
            catch (Exception e) {
                WindowManager.resetToConnectMenu();
                return;
            }

            ObjectMapper objectMapper = new ObjectMapper();

            try {
                packet = objectMapper.readValue(message, Packet.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            switch (packet.getCommand()){
                case "closeConnection":
                    WindowManager.resetToConnectMenu();
                    return;
                case "availableClients":
                    updateAvailableClientsList(packet.getData());
                    break;
                case "transferScreen":
                    break;
            }
        }
    }

    private void updateAvailableClientsList(String availableClients) {
        String[] clientList = availableClients.split("\\|");
        ArrayList<String> nameToAdd = new ArrayList<>();

        ArrayList<String> availableClientsArray = new ArrayList<>(Arrays.asList(ConnectionHandler.instance.availableClients));

        for (String client : clientList) {

            if (!availableClientsArray.contains(client)) {
                nameToAdd.add(client);
            }
        }

        if (!nameToAdd.isEmpty()) {
            availableClientsArray.addAll(nameToAdd);
            ConnectionHandler.instance.availableClients = availableClientsArray.toArray(new String[0]);
        }
    }
}
