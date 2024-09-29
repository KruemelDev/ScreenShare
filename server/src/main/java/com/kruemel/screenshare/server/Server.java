package com.kruemel.screenshare.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	
    public static void main(String[] args){
        ServerSocket server;

        try {
            server = new ServerSocket();
            server.setReuseAddress(true);
            server.bind(new InetSocketAddress("0.0.0.0", 45555));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        while(true) {
    		try {

                Socket socket = server.accept();
                DataInputStream in = new DataInputStream(socket.getInputStream());
                String name = in.readUTF();
                System.out.println(name);
                if (ConnectionHandler.DuplicateName(name) || name.contains("|")){
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    out.writeUTF("close");
                    socket.close();
                    continue;
                }
                ClientData client = new ClientData(name, socket);
                ConnectionHandler.clients.add(client);
                
                Thread clientHandler = new Thread(new ConnectionHandler(client));
                clientHandler.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
    	}
    	
        
        
    }
}