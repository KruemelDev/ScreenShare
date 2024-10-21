package com.kruemel.screenshare.client;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kruemel.screenshare.dto.Packet;

public class CommandHandler implements Runnable {

    @Override
    public void run() {
        String message = "";
        Packet packet;
        while (true) {
            try {
                message = ConnectionHandler.instance.in.readUTF();

            } catch (SocketException e) {
                Client.resetToConnectMenu();

            }
            catch (EOFException e){
                Client.resetToConnectMenu();
                break;
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }

            ObjectMapper objectMapper = new ObjectMapper();

            try {
                packet = objectMapper.readValue(message, Packet.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            switch (packet.getCommand()){
                case "error":
                    WindowManager.instance.ShowErrorMessage(packet.getData(), packet.getCommand());
                    break;
                case "closeConnection":
                    Client.resetToConnectMenu();
                    WindowManager.instance.ShowErrorMessage("Try to reconnect under a different name", "Connection closed");
                    return;
                case "availableClients":
                    updateAvailableClientsList(packet.getData());
                    break;
                case "sharedScreenRequest":
                    WindowManager.instance.RequestForTransferScreenPopUp(packet.getData());
                    break;
                case "sharedScreenStop":
                    ConnectionHandler.instance.screenShare = false;
                case "getSharedScreen":
                    String base64ImagePiece = packet.getData();
                    if(base64ImagePiece.equals("fullImage") && WindowManager.instance.watchScreenAllow){
                        WindowManager.instance.ScreenShareDisplay(ConnectionHandler.instance.base64ImagePiece);
                        ConnectionHandler.instance.base64ImagePiece = "";
                    }
                    else{
                        ConnectionHandler.instance.base64ImagePiece += base64ImagePiece;
                    }
                    break;
            }
        }
    }

    private void updateAvailableClientsList(String availableClients) {
        ConnectionHandler.instance.availableClients = availableClients.split("\\|");
    }

}
