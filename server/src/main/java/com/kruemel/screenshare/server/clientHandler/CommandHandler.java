package com.kruemel.screenshare.server.clientHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.kruemel.screenshare.server.ConnectionHandler;

import java.util.ArrayList;
import java.util.Objects;

import static com.kruemel.screenshare.dto.Util.dataToJson;

public class CommandHandler {

    private ClientData client;
    public CommandHandler(ClientData client) {
        this.client = client;
    }

    public void SendScreen(String base64Image) {
        int chunkSize = 1024;
        ArrayList<ClientData> clientsToRemove = new ArrayList<>();

        synchronized (client.screenShareAllowed){
            for (ClientData c : client.screenShareAllowed) {

                if(!ConnectionHandler.ClientOnline(c.name)){
                    clientsToRemove.add(c);
                    continue;
                }
                for (int i = 0; i < base64Image.length(); i += chunkSize) {

                    String chunk = base64Image.substring(i, Math.min(i + chunkSize, base64Image.length()));

                    c.WriteMessage(dataToJson("getSharedScreen", chunk));

                }
                c.WriteMessage(dataToJson("getSharedScreen", "fullImage"));

            }
        }

        for (ClientData c : clientsToRemove) {
            synchronized (client.screenShareAllowed){
                this.client.screenShareAllowed.remove(c);
            }

        }
    }

    public void GetScreenHandler(String base64Image) {
        if(base64Image == null) {
            this.client.base64ImagePiece = "";
            return;
        }
        synchronized (this.client){
            String base64ImagePiece = base64Image.replaceAll("[\\p{Cntrl}]", "");

            if(base64ImagePiece.equals("fullImage")){
                SendScreen(this.client.base64ImagePiece);
                this.client.base64ImagePiece = "";
            }
            else{
                this.client.base64ImagePiece += base64ImagePiece;
            }
        }
    }
    public void StopWatchingScreen(){
        if(this.client.currentScreenWatching == null){
            return;
        }
        synchronized (this.client.currentScreenWatching.screenShareAllowed) {
            this.client.currentScreenWatching.screenShareAllowed.remove(this.client);
        }
        synchronized (this.client.currentScreenWatching.screenShareAllowed){
            if (this.client.currentScreenWatching.screenShareAllowed.isEmpty()) {

                this.client.currentScreenWatching.WriteMessage(dataToJson("sharedScreenStop"));
                this.client.currentScreenWatching.base64ImagePiece = "";
            }
        }

        this.client.currentScreenWatching = null;
    }
    public void StopScreenShare(){
        synchronized (this.client.screenShareAllowed){
            ArrayList<ClientData> clientsToRemove = new ArrayList<>();
            for(ClientData c : this.client.screenShareAllowed){
                clientsToRemove.add(c);
                c.WriteMessage(dataToJson("error", "Der Host hat das teilen beendet"));
            }
            for(ClientData c : clientsToRemove){
                this.client.screenShareAllowed.remove(c);
            }
        }
    }

    public void AcceptScreenShare(String allowedClientName){
        ClientData allowedClient = GetClientByName(allowedClientName);
        if(allowedClient == null){
            return;
        }
        if(this.client.screenShareAllowed.contains(allowedClient)){
            return;
        }
        allowedClient.currentScreenWatching = this.client;
        synchronized (this.client.screenShareAllowed){
            this.client.screenShareAllowed.add(allowedClient);
        }
    }

    public void ScreenShareRequest(ClientData clientDataTarget, String requestClientName) {
        if(Objects.equals(clientDataTarget.name, requestClientName)){
            clientDataTarget.WriteMessage(dataToJson("error", "You cannot share your screen with yourself"));
            return;
        }

        clientDataTarget.WriteMessage(dataToJson("sharedScreenRequest", requestClientName));
    }
    public ClientData GetClientByName(String name) {
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

        this.client.WriteMessage(dataToJson("availableClients", finalMessage));
    }

}
