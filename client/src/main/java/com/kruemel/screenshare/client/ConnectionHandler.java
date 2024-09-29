package com.kruemel.screenshare.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

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
            //TODO write to packet
            WriteMessage("closeConnection");
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public void RequestAvailableClients(){
        Packet getClientsPacket = new Packet("getClients", "");
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
            WindowManager.resetToConnectMenu();
        }

    }


}
