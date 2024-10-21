package com.kruemel.screenshare.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kruemel.screenshare.dto.Packet;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConnectionHandler {
    public Socket socket;
    public DataInputStream in;
    public DataOutputStream out;


    public String[] availableClients = new String[0];
    public static ConnectionHandler instance;

    public volatile boolean screenShare = false;
    public String base64ImagePiece = "";

    public ConnectionHandler(){
        instance = this;
    }
    public void ConnectToServer(String ip, int port, String name){
        if(socket == null){
            try{
                socket = new Socket(ip, port);
                this.in = new DataInputStream(socket.getInputStream());
                this.out = new DataOutputStream(socket.getOutputStream());

                Packet packet = new Packet("name", name);
                ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                String json = ow.writeValueAsString(packet);
                WriteMessage(json);

                Thread commandHandlerThread = new Thread(new CommandHandler());
                commandHandlerThread.start();
                WindowManager.instance.frame.getContentPane().removeAll();
                WindowManager.instance.frame.repaint();
                RequestAvailableClients();
                WindowManager.instance.ScreenShareConnectionMenu();
            }
            catch(IOException e){
                WindowManager.instance.ShowErrorMessage("Es konnte keine Verbindung zum Server aufgebaut werden", "Verbindungsprobleme");
                socket = null;
                return;
            }
            catch(NullPointerException e){
                return;
            }

        }
        else{
            return;
        }

    }

    synchronized public void CloseConnection(){
        if (socket != null){
            Packet packet = new Packet("closeConnection");
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json;
            try {
                json = ow.writeValueAsString(packet);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            WriteMessage(json);
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
    public void StopWatchingScreen(){
        Packet requestScreenShareAcceptPackage = new Packet("stopScreen");
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json;
        try {
            json = ow.writeValueAsString(requestScreenShareAcceptPackage);
        } catch(JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        WriteMessage(json);

    }

    public void ScreenShareStart(){
        ShareScreen screenShare = new ShareScreen();
        Thread shareScreenThread = new Thread(screenShare);
        this.screenShare = true;
        System.out.println("start screen share");
        shareScreenThread.start();
    }

    public void SendScreenShareAcception(String name){
        Packet requestScreenShareAcceptPackage = new Packet("acceptScreenShare", name);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json;
        try {
            json = ow.writeValueAsString(requestScreenShareAcceptPackage);
        } catch(JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        WriteMessage(json);
    }

    public void RequestScreenShare(String target) {
        Packet requestScreenSharePackage = new Packet("requestScreenShare", target);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json;
        try {
            json = ow.writeValueAsString(requestScreenSharePackage);
        } catch(JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        WriteMessage(json);
    }

    public void RequestAvailableClients(){
        Packet getClientsPacket = new Packet("getClients");
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json;
        try {
            json = ow.writeValueAsString(getClientsPacket);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        WriteMessage(json);
    }


    public void WriteMessage(String message) {
        try {
            if (out != null) {
                out.writeUTF(message);
                out.flush();
            }
        } catch (IOException e) {

            WindowManager.instance.ShowErrorMessage(
                    "Lost connection to server",
                    "Connection Error"
            );
            ConnectionHandler.instance.screenShare = false;
            Client.resetToConnectMenu();
        }

    }


}
