package com.kruemel.screenshare.client;

import java.io.EOFException;
import java.io.IOException;

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
                Client.resetToConnectMenu();
                break;
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
            catch (Exception e) {
                Client.resetToConnectMenu();
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
                    Client.resetToConnectMenu();
                    WindowManager.instance.ShowErrorMessage("Try to reconnect under a different name", "Connection closed");
                    return;
                case "availableClients":
                    updateAvailableClientsList(packet.getData());
                    break;

                case "transferScreenRequest":
                    break;
                case "transferScreenStop":
                    break;
                case "transferScreen":
                    break;
            }
        }
    }

    private void updateAvailableClientsList(String availableClients) {
        ConnectionHandler.instance.availableClients = availableClients.split("\\|");
    }

}
