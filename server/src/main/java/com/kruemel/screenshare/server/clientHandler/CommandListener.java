package com.kruemel.screenshare.server.clientHandler;

import java.io.EOFException;
import java.io.IOException;

import java.net.SocketException;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.kruemel.screenshare.dto.Packet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kruemel.screenshare.server.ConnectionHandler;

import static com.kruemel.screenshare.dto.Util.*;

public class CommandListener implements Runnable{
    public ClientData client;
    public CommandListener(ClientData client){
        this.client = client;
    }

    @Override
    public void run() {
        CommandListener();
    }

    public void CommandListener(){
        String message;
        Packet packet;
        CommandHandler commandHandler = new CommandHandler(this.client);
        while(!this.client.error){
            try {
                message = this.client.in.readUTF();
            }
            catch (SocketException e){
                this.client.error = true;
                return;
            }
            catch (EOFException e){
                ConnectionHandler.removeClient(this.client);
                this.client.error = true;
                return;
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
            ObjectMapper objectMapper = new ObjectMapper();

            try {
                packet = objectMapper.readValue(message, Packet.class);

            } catch(JsonParseException e){
                ConnectionHandler.removeClient(this.client);
                this.client.error = true;
                return;
            }

            catch (JsonProcessingException e) {
                this.client.WriteMessage(dataToJson("closeConnection"));
                this.client.error = true;
                return;
            }
            switch (packet.getCommand()){
                case "getClients":
                    commandHandler.SendAvailableClients();
                    break;
                case "closeConnection":
                    ConnectionHandler.removeClient(client);
                    return;
                case "requestScreenShare":
                    String target = packet.getData();
                    ClientData clientData = commandHandler.GetClientByName(target);
                    if(clientData != null){
                        commandHandler.ScreenShareRequest(clientData, client.name);
                    }
                    break;
                case "acceptScreenShare":
                    commandHandler.AcceptScreenShare(packet.getData());
                    break;
                case "getScreen":
                    commandHandler.GetScreenHandler(packet.getData());
                    break;

                case "stopWatching":
                    commandHandler.StopWatchingScreen();
                    break;
                case "sharedScreenStop":
                    commandHandler.StopScreenShare();
                    break;
            }
        }
        cleanUp();
        commandHandler = null;
        System.gc();
    }

    private void cleanUp(){
        if(this.client.socket != null){
            try {
                this.client.socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        this.client = null;

    }
}
